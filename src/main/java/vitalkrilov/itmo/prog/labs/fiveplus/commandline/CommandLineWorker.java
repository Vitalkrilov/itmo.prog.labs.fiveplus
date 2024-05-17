package vitalkrilov.itmo.prog.labs.fiveplus.commandline;

import vitalkrilov.itmo.prog.labs.fiveplus.collection.CollectionManager;
import vitalkrilov.itmo.prog.labs.fiveplus.dataclasses.EyeColor;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Logger;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Result;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.TextFormatter;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.Utils;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Manages all non-collection-specific commands and logic.
 */
public class CommandLineWorker {
    private final static int MAX_STDIN_RETRIES_WHEN_IO_ERROR = 3;

    /**
     * Command class.
     */
    private static class Command {
        final String name;
        final String description;
        enum ArgumentType {
            /**
             * Argument which has specific position in command line.
             */
            POSITIONAL,
            /**
             * Argument that must not be in command line but will be prompted during execution.
             */
            QUERY,
            /**
             * Argument that will be at every position from its starting one until the end of command line.
             */
            MULTIPLE
        }

        /**
         * Argument class.
         */
        static class Argument {
            final ArgumentType type;
            final boolean isOptional;
            final String name;
            final String description;

            /**
             * Creates new argument.
             * @param type Argument type.
             * @param name Argument name.
             * @param description Argument description.
             */
            public Argument(ArgumentType type, String name, String description) {
                this(type, false, name, description);
            }

            /**
             * Creates new argument.
             * @param type Argument type.
             * @param isOptional Argument optionality.
             * @param name Argument name.
             * @param description Argument description.
             */
            public Argument(ArgumentType type, boolean isOptional, String name, String description) {
                this.type = type;
                this.isOptional = isOptional;
                this.name = name;
                this.description = description;
            }
        }
        final Map<ArgumentType, List<Argument>> arguments; //FEATURE: [STABILITY]: not map but lists with order check

        /**
         * Creates new command.
         * @param name Command name.
         * @param description Command description.
         * @param arguments Command arguments.
         */
        Command(String name, String description, Argument... arguments) {
            this.name = name;
            this.description = description;
            this.arguments = new HashMap<>();
            for (Argument a : arguments) {
                if (!this.arguments.containsKey(a.type))
                    this.arguments.put(a.type, new ArrayList<>());
                this.arguments.get(a.type).add(a);
            }
        }

        /**
         * Formats usage line.
         * @return Usage line.
         */
        private String getUsageLine() {
            StringBuilder sb = new StringBuilder();
            sb.append(this.name);
            for (Argument a : this.arguments.getOrDefault(ArgumentType.POSITIONAL, new ArrayList<>())) {
                sb.append(" ");
                if (a.isOptional) sb.append('[');
                sb.append(a.name);
                if (a.isOptional) sb.append(']');
            }
            for (Argument a : this.arguments.getOrDefault(ArgumentType.MULTIPLE, new ArrayList<>())) {
                sb.append(" ");
                if (a.isOptional) sb.append('[');
                sb.append(a.name);
                sb.append("...");
                if (a.isOptional) sb.append(']');
            }
            for (Argument a : this.arguments.getOrDefault(ArgumentType.QUERY, new ArrayList<>())) {
                sb.append(" {");
                if (a.isOptional) sb.append('[');
                sb.append(a.name);
                if (a.isOptional) sb.append(']');
                sb.append("}");
            }
            return sb.toString();
        }

        /**
         * Formats usage message.
         * @return Usage message.
         */
        private String getUsage() {
            StringBuilder sb = new StringBuilder();
            sb.append("Usage: ");
            sb.append(getUsageLine());
            return sb.toString();
        }

        /**
         * Formats help message.
         * @return Help message.
         */
        private String getHelp() {
            StringBuilder sb = new StringBuilder();
            sb.append(getUsage());
            sb.append("\n\n");
            sb.append(this.description);

            int maxNameLength = 0;
            for (ArgumentType at : this.arguments.keySet())
                for (Argument a : this.arguments.get(at))
                    if (maxNameLength < a.name.length())
                        maxNameLength = a.name.length();
            maxNameLength += 2; // add default spacing

            for (ArgumentType at : this.arguments.keySet()) {
                List<Argument> la = this.arguments.get(at);
                if (la.isEmpty()) continue;
                sb.append("\n\n");
                sb.append(switch (at) {
                    case POSITIONAL -> "Positional arguments";
                    case MULTIPLE -> "Multiple arguments";
                    case QUERY -> "Query arguments";
                });
                sb.append(":");
                for (Argument a : this.arguments.get(at)) {
                    sb.append("\n  ");
                    sb.append(a.name);
                    sb.append(" ".repeat(maxNameLength - a.name.length()));
                    sb.append(a.description);
                }
            }
            return sb.toString();
        }
    }

    private final static LinkedHashMap<String, Command> commandsDatabase;

    static {
        commandsDatabase = new LinkedHashMap<>();
        Command c;

        c = new Command("help", "Вывести справку по доступным командам.",
                new Command.Argument(Command.ArgumentType.POSITIONAL, true, "command", "название команды для вывода подробной помощи только по ней"));
        commandsDatabase.put(c.name, c);

        c = new Command("info", "Вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.).");
        commandsDatabase.put(c.name, c);

        c = new Command("show", "Вывести в стандартный поток вывода все элементы коллекции.");
        commandsDatabase.put(c.name, c);

        c = new Command("add", "Добавить новый элемент в коллекцию.",
                new Command.Argument(Command.ArgumentType.QUERY, "element", "элемент Person"));
        commandsDatabase.put(c.name, c);

        c = new Command("update", "Обновить значение элемента коллекции, id которого равен заданному.",
                new Command.Argument(Command.ArgumentType.POSITIONAL, "id", "id обновляемого элемента"),
                new Command.Argument(Command.ArgumentType.QUERY, "element", "элемент Person"));
        commandsDatabase.put(c.name, c);

        c = new Command("remove_by_id", "Удалить элемент из коллекции по его id.",
                new Command.Argument(Command.ArgumentType.POSITIONAL, "id", "id удаляемого элемента"));
        commandsDatabase.put(c.name, c);

        c = new Command("clear", "Очистить коллекцию.");
        commandsDatabase.put(c.name, c);

        c = new Command("save", "Сохранить коллекцию в файл.");
        commandsDatabase.put(c.name, c);

        c = new Command("execute_script", "Считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.",
                new Command.Argument(Command.ArgumentType.POSITIONAL, "file_name", "путь к скрипту"));
        commandsDatabase.put(c.name, c);

        c = new Command("exit", "Завершить текущую сессию (без сохранения в файл).");
        commandsDatabase.put(c.name, c);

        c = new Command("stop", "Завершить программу (без сохранения в файл).");
        commandsDatabase.put(c.name, c);

        c = new Command("remove_first", "Удалить первый элемент из коллекции.");
        commandsDatabase.put(c.name, c);

        c = new Command("remove_head", "Вывести первый элемент коллекции и удалить его.");
        commandsDatabase.put(c.name, c);

        c = new Command("add_if_max", "Добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции.",
                new Command.Argument(Command.ArgumentType.QUERY, "element", "элемент Person"));
        commandsDatabase.put(c.name, c);

        c = new Command("count_greater_than_eye_color", "Вывести количество элементов, значение поля eyeColor которых больше заданного.",
                new Command.Argument(Command.ArgumentType.POSITIONAL, "eyeColor", String.format("строковое значение EyeColor; доступные варианты: %s", Arrays.toString(EyeColor.values()))));
        commandsDatabase.put(c.name, c);

        c = new Command("filter_starts_with_name", "Вывести элементы, значение поля name которых начинается с заданной подстроки.");
        commandsDatabase.put(c.name, c);

        c = new Command("print_field_descending_height", "Вывести значения поля height всех элементов в порядке убывания.");
        commandsDatabase.put(c.name, c);

        c = new Command("shell", "Запустить командную оболочку.",
                new Command.Argument(Command.ArgumentType.POSITIONAL, true, "filename", "путь к скрипту для запуска"));
        commandsDatabase.put(c.name, c);

        c = new Command("echo", "Вывести строку текста.",
                new Command.Argument(Command.ArgumentType.MULTIPLE, true, "text", "текст для вывода"));
        commandsDatabase.put(c.name, c);

        c = new Command("cat", "Объединить файлы и вывести их в стандартный поток вывода.",
                new Command.Argument(Command.ArgumentType.MULTIPLE, true, "filenames", "пути к файлам (\"-\" используется для стандартного потока ввода)"));
        commandsDatabase.put(c.name, c);
    }

    CollectionManager collectionManager; //TODO: lab6: connection data should be here

    static class Session {
        final Reader reader;
        final String scriptFilename;
        final String userAliasFilename;
        boolean unloadRequested;

        /**
         * For interactive
         * @param reader Container of data to read from.
         */
        Session(Reader reader) {
            this.reader = reader;
            this.scriptFilename = null;
            this.userAliasFilename = null;
            this.unloadRequested = false;
        }

        /**
         * For scripts
         * @param reader Container of data to read from.
         */
        Session(Reader reader, String scriptFilename, String userAliasFilename) {
            this.reader = reader;
            this.scriptFilename = scriptFilename;
            this.userAliasFilename = userAliasFilename;
            this.unloadRequested = false;
        }

        /**
         * Checks if it's interactive (has assigned file).
         * @return result
         */
        boolean isInteractive() {
            return this.scriptFilename == null;
        }
    }
    Stack<Session> sessions;

    /**
     * Creates new command line worker.
     * @param collectionManager Link to collection manager.
     */
    public CommandLineWorker(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
        this.sessions = new Stack<>();
    }

    /**
     * Checks if any session is running.
     * @return result
     */
    private boolean isRunning() {
        return !this.sessions.isEmpty();
    }

    /**
     * Checks if current session is interactive.
     * @return result
     */
    boolean isInteractiveNow() {
        return this.sessions.peek().isInteractive();
    }

    /**
     * Applies `path` to current path.
     * @param path Part of full path that should be added.
     * @return New path (or error if "/" is currently used as string somehow...).
     */
    Result<String, String> resolvePath(String path) {
        if (path.isEmpty()) {
            return new Result.Ok<>("");
        } else if (path.charAt(0) == '/') {
            return new Result.Ok<>(path);
        } else {
            Session lastNonInteractive = null;
            for (int i = this.sessions.size() - 1; i >= 0; --i) {
                lastNonInteractive = this.sessions.elementAt(i);
                if (!lastNonInteractive.isInteractive())
                    break;
            }
            if (lastNonInteractive == null || lastNonInteractive.isInteractive()) {
                return new Result.Ok<>(path);
            } else {
                Path currentDir = Path.of(lastNonInteractive.scriptFilename).toAbsolutePath().getParent();
                if (currentDir == null)
                    return new Result.Err<>("Tried to get out of filesystem's root");
                return new Result.Ok<>(currentDir.resolve(path).toString());
            }
        }
    }

    /**
     * Starts interactive session.
     * This must be called only once per running instance.
     * @param args Guaranteed to have size > 1: executable name, arguments
     */
    public void start(List<String> args) {
        if (isRunning())
            throw new RuntimeException("Restarting is prohibited if previous is still running.");

        //TODO: lab6: When it will be client, it should connect to server here. (or it will be session-per-command-like?)

        this.sessions.push(new Session(new InputStreamReader(System.in)));
        Set<String> scriptsNameFastSearch = new HashSet<>(); // For fast searching
        int stdinRetriesLeft = MAX_STDIN_RETRIES_WHEN_IO_ERROR;

        Logger.logInfo("%s's CLI Worker is ready to process user input...", args.get(0));

        lineLoop:
        while (true) {
            while (this.sessions.peek().unloadRequested) {
                Session s = this.sessions.pop();
                if (this.sessions.isEmpty())
                    break lineLoop;
                if (!s.isInteractive())
                    scriptsNameFastSearch.remove(s.scriptFilename);

                // Restore count of retries (there are no cases where it is not required by its restoring design!)
                if (isInteractiveNow())
                    stdinRetriesLeft = MAX_STDIN_RETRIES_WHEN_IO_ERROR;
            }

            if (isInteractiveNow())
                System.out.printf("%s> ", args.get(0));

            List<String> command = new ArrayList<>();
            {
                CommandLineParser.PCLResult res = CommandLineParser.parseCommand(this.sessions.peek().reader, command);
                if (res == CommandLineParser.PCLResult.ERR_IO_ERROR) {
                    if (isInteractiveNow()) {
                        Logger.logError("An IO error occurred while reading input from console.");
                        if (--stdinRetriesLeft == 0)
                            this.sessions.peek().unloadRequested = true;
                    } else {
                        Logger.logError("An IO error occurred while reading input from script (%s).", this.sessions.peek().userAliasFilename);
                        this.sessions.peek().unloadRequested = true;
                    }
                    continue;
                } else if (isInteractiveNow())
                    stdinRetriesLeft = MAX_STDIN_RETRIES_WHEN_IO_ERROR; // Reset retries if succeed

                // Put newline when hit ^D in CLI
                if (res == CommandLineParser.PCLResult.ERR_END_OF_FILE) {
                    if (isInteractiveNow()) {
                        System.out.println();

                        // Hotkey silent exit to ^D hits in CLI
                        if (command.isEmpty())
                            this.sessions.peek().unloadRequested = true;
                    } else {
                        // Script ended -- need to unload after execution of last command (if any)
                        this.sessions.peek().unloadRequested = true;
                    }
                }
            }

            Result<Object, String> res = new Result.Ok<>();
            if (!command.isEmpty()) {
                switch (command.get(0)) {
                    case "help": {
                        if (command.size() > 2) {
                            res = new Result.Err<>("Invalid arguments count");
                            break;
                        }

                        if (command.size() == 2) {
                            Command c = commandsDatabase.get(command.get(1));
                            if (c == null) {
                                res = new Result.Err<>(String.format("Command not found: %s", command.get(1)));
                                break;
                            }
                            System.out.println(c.getHelp());
                            break;
                        }

                        System.out.printf("Available commands:%n");

                        LinkedHashMap<String, String> cachedUsageLines = new LinkedHashMap<>();
                        int maxNameLength = 0;
                        for (String name : commandsDatabase.keySet()) {
                            String usageLine = commandsDatabase.get(name).getUsageLine();
                            if (maxNameLength < usageLine.length())
                                maxNameLength = usageLine.length();
                            cachedUsageLines.put(name, usageLine);
                        }
                        maxNameLength += 2; // add default spacing

                        for (String name : cachedUsageLines.keySet()) {
                            String usageLine = cachedUsageLines.get(name);
                            System.out.printf("  %s%s%s%n", usageLine, " ".repeat(maxNameLength - usageLine.length()), commandsDatabase.get(name).description);
                        }
                        break;
                    }
                    case "info":
                        res = new SubprogramInfo(this).execute(command);
                        break;
                    case "show":
                        res = new SubprogramShow(this).execute(command);
                        break;
                    case "add":
                        res = new SubprogramAdd(this).execute(command);
                        break;
                    case "update":
                        res = new SubprogramUpdate(this).execute(command);
                        break;
                    case "remove_by_id":
                        res = new SubprogramRemoveById(this).execute(command);
                        break;
                    case "clear":
                        res = new SubprogramClearCollection(this).execute(command);
                        break;
                    case "save":
                        res = new SubprogramSaveCollection(this).execute(command);
                        break;
                    case "execute_script":
                    case "shell": {
                        if (command.size() == 1 && command.get(0).equals("shell")) {
                            this.sessions.push(new Session(new InputStreamReader(System.in)));
                            break;
                        }
                        if (command.size() != 2) {
                            res = new Result.Err<>("Invalid arguments count");
                            break;
                        }

                        String filename = command.get(1);

                        var resolvePathRes = this.resolvePath(filename);
                        if (resolvePathRes.isErr()) {
                            res = new Result.Err<>(resolvePathRes.getErr());
                            break;
                        }

                        String extendedFilename = resolvePathRes.getOk();

                        var getFileRes = Utils.getFile(Utils.GFType.READ, extendedFilename);
                        if (getFileRes.isErr()) {
                            res = new Result.Err<>(getFileRes.getErr());
                            break;
                        }
                        File file = getFileRes.getOk();

                        // Okay. Since there are *many* people who dislike recursion, then here it is.
                        if (scriptsNameFastSearch.contains(file.getAbsolutePath())) {
                            res = new Result.Err<>("Attempt to invoke same script twice rejected");
                            break;
                        }

                        try {
                            FileInputStream fis = new FileInputStream(file);
                            BufferedInputStream bis = new BufferedInputStream(fis);
                            this.sessions.push(new Session(new InputStreamReader(bis), file.getAbsolutePath(), filename));
                            scriptsNameFastSearch.add(file.getAbsolutePath());
                        } catch (FileNotFoundException e) {
                            Logger.logError("Race condition detected."); // Because we have already checked this file for existence
                            res = new Result.Err<>("File not found");
                        }
                        break;
                    }
                    case "exit": {
                        if (command.size() != 1) {
                            res = new Result.Err<>("Invalid arguments count");
                            break;
                        }

                        this.sessions.peek().unloadRequested = true;
                        break;
                    }
                    case "stop": {
                        if (command.size() != 1) {
                            res = new Result.Err<>("Invalid arguments count");
                            break;
                        }

                        for (Session s : this.sessions)
                            s.unloadRequested = true;
                        break;
                    }
                    case "remove_first":
                        res = new SubprogramRemoveFirst(this).execute(command);
                        break;
                    case "remove_head":
                        res = new SubprogramRemoveHead(this).execute(command);
                        break;
                    case "add_if_max":
                        res = new SubprogramAddIfMax(this).execute(command);
                        break;
                    case "count_greater_than_eye_color":
                        res = new SubprogramCountGreaterThanEyeColor(this).execute(command);
                        break;
                    case "filter_starts_with_name":
                        res = new SubprogramPrintNameStartsWith(this).execute(command);
                        break;
                    case "print_field_descending_height":
                        res = new SubprogramPrintHeightDescending(this).execute(command);
                        break;
                    case "echo": {
                        if (command.size() > 1)
                            System.out.print(command.get(1));
                        for (int i = 2; i < command.size(); ++i) {
                            System.out.print(' ');
                            System.out.print(command.get(i));
                        }
                        System.out.println();
                        break;
                    }
                    case "cat": {
                        List<String> listToIterate;
                        int listBeginning;
                        if (command.size() < 2) {
                            listToIterate = List.of("-");
                            listBeginning = 0;
                        } else {
                            listToIterate = command;
                            listBeginning = 1;
                        }
                        boolean lastCharIsNewline = true;
                        for (int i = listBeginning; i < listToIterate.size(); ++i) {
                            Reader r;
                            if (listToIterate.get(i).equals("-")) {
                                r = new InputStreamReader(System.in);
                            } else {
                                String filename = listToIterate.get(i);

                                var resolvePathRes = this.resolvePath(filename);
                                if (resolvePathRes.isErr()) {
                                    res = new Result.Err<>(resolvePathRes.getErr());
                                    break;
                                }

                                String extendedFilename = resolvePathRes.getOk();

                                var getFileRes = Utils.getFile(Utils.GFType.READ, extendedFilename);
                                if (getFileRes.isErr()) {
                                    res = new Result.Err<>(getFileRes.getErr());
                                    break;
                                }
                                File file = getFileRes.getOk();

                                try {
                                    FileInputStream fis = new FileInputStream(file);
                                    BufferedInputStream bis = new BufferedInputStream(fis);
                                    r = new InputStreamReader(bis);
                                } catch (FileNotFoundException e) {
                                    Logger.logError("Race condition detected."); // Because we have already checked this file for existence
                                    res = new Result.Err<>("File not found");
                                    break;
                                }
                            }
                            try {
                                int c;
                                while ((c = r.read()) != -1) {
                                    System.out.print((char)c);
                                    lastCharIsNewline = c == '\n';
                                }
                            } catch (IOException e) {
                                res = new Result.Err<>("Reading IO error");
                            }
                        }
                        if (!lastCharIsNewline)
                            System.out.println();
                        break;
                    }
                    default: {
                        Logger.printError("%s: command not found: %s", args.get(0), command.get(0));
                        break;
                    }
                }
            }
            if (res.isErr()) {
                if (!this.isInteractiveNow()) {
                    Logger.printError(":: in script `%s`:", this.sessions.peek().userAliasFilename);
                }
                if (res.getErr().equals("Invalid arguments count")) {
                    Logger.printError("%s: error: %s", command.get(0), TextFormatter.startWithLowercase(res.getErr()));
                    Logger.printError("%s", commandsDatabase.get(command.get(0)).getUsage());
                } else {
                    Logger.printError("%s: error: %s", command.get(0), TextFormatter.startWithLowercase(res.getErr()));
                }
                if (!this.isInteractiveNow()) {
                    Logger.printError(":: unloading script...");
                    this.sessions.peek().unloadRequested = true;
                }
            }
        }
    }
}
