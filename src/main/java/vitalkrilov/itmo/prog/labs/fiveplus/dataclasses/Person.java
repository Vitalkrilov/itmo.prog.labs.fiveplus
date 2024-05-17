package vitalkrilov.itmo.prog.labs.fiveplus.dataclasses;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.*;

import java.io.Reader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

public class Person implements ReaderFillable, Cloneable, Comparable<Person> {
    private @NotNull Integer id; //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private @NotNull String name; //Поле не может быть null, Строка не может быть пустой
    private @NotNull Coordinates coordinates; //Поле не может быть null
    private @NotNull java.time.LocalDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private long height; //Значение поля должно быть больше 0
    private @Nullable EyeColor eyeColor; //Поле может быть null
    private @Nullable HairColor hairColor; //Поле может быть null
    private @NotNull Country nationality; //Поле не может быть null
    private @Nullable Location location; //Поле может быть null

    public Person() {
        this.id = 1;
        this.name = "Unknown";
        this.coordinates = new Coordinates();
        this.creationDate = LocalDateTime.now();
        this.height = 1;
        this.eyeColor = null;
        this.hairColor = null;
        this.nationality = Country.RUSSIA;
        this.location = null;
    }

    public Integer getId() {
        return id;
    }

    public Result<Object, String> setId(Integer id) {
        if (id <= 0)
            return new Result.Err<>("ID must be more than 0");
        this.id = id;
        return new Result.Ok<>();
    }

    public String getName() {
        return name;
    }

    private final static Pattern SET_NAME_REGEX_PATTERN = Pattern.compile("^\\p{Lu}\\p{L}+([\\s-]\\p{L}+)*$", Pattern.UNICODE_CHARACTER_CLASS);

    public Result<Object, String> setName(String name) {
        if (name == null)
            return new Result.Err<>("Name can't be null");
        if (name.isEmpty())
            return new Result.Err<>("Name can't be empty");
        if (!SET_NAME_REGEX_PATTERN.matcher(name).find())
            return new Result.Err<>("Name must contain words (first word contains more than 1 letter and starts with upper case letter; space or '-' between words)");

        this.name = name;
        return new Result.Ok<>();
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public long getHeight() {
        return height;
    }

    public Result<Object, String> setHeight(long height) {
        if (height <= 0)
            return new Result.Err<>("Value must be greater than 0");
        if (height > 300)
            return new Result.Err<>("Got impossible height for person. Expecting in (0; 300]");
        this.height = height;
        return new Result.Ok<>();
    }

    public EyeColor getEyeColor() {
        return eyeColor;
    }

    public Result<Object, String> setEyeColor(EyeColor eyeColor) {
        this.eyeColor = eyeColor;
        return new Result.Ok<>();
    }

    public HairColor getHairColor() {
        return hairColor;
    }

    public Result<Object, String> setHairColor(HairColor hairColor) {
        this.hairColor = hairColor;
        return new Result.Ok<>();
    }

    public Country getNationality() {
        return nationality;
    }

    public Result<Object, String> setNationality(Country nationality) {
        if (nationality == null)
            return new Result.Err<>("Nationality can't be null");
        this.nationality = nationality;
        return new Result.Ok<>();
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Result<Object, String> fillFromReader(boolean isInteractive, Reader reader, int spacingLevel) {
        NullableLineInputProcessor nlip = new NullableLineInputProcessor(reader);

        while (true) {
            var res = nlip.collectInteractively(isInteractive, spacingLevel, "Person's name (String, not null; first word contains more than 1 letter and starts with upper case letter; space or '-' between words)", false, s -> new Result.Ok<>(s));
            if (res.isErr()) return new Result.Err<>(res.getErr());

            var setterRes = res.andThen(this::setName);
            if (setterRes.isOk())
                break;
            System.out.printf("%s%s%n", " ".repeat(spacingLevel + 2), setterRes.getErr());
        }

        {
            if (isInteractive)
                System.out.printf("%sEnter Person's Coordinates:%n", " ".repeat(2 * spacingLevel));
            Coordinates coordinates = new Coordinates();
            var res = coordinates.fillFromReader(isInteractive, reader, spacingLevel + 1);
            if (res.isErr()) return new Result.Err<>(res.getErr());
            this.coordinates = coordinates;
        }

        while (true) {
            var res = nlip.collectInteractively(isInteractive, spacingLevel, "Person's height (long, > 0, <= 300)", false, ValueParser::parseLong);
            if (res.isErr()) return new Result.Err<>(res.getErr());

            var setterRes = res.andThen(this::setHeight);
            if (setterRes.isOk())
                break;
            System.out.printf("%s%s%n", " ".repeat(spacingLevel + 2), setterRes.getErr());
        }

        {
            var res = nlip.collectInteractively(isInteractive, spacingLevel, String.format("Person's eyeColor (%s%s, can be null)", EyeColor.class.getSimpleName(), Arrays.asList(EyeColor.values())), true, s -> {
                try {
                    return new Result.Ok<>(EyeColor.valueOf(s));
                } catch (IllegalArgumentException e) {
                    return new Result.Err<>("Incorrect value");
                }
            }).andThen(this::setEyeColor);
            if (res.isErr()) return res;
        }

        {
            var res = nlip.collectInteractively(isInteractive, spacingLevel, String.format("Person's hairColor (%s%s, can be null)", HairColor.class.getSimpleName(), Arrays.asList(HairColor.values())), true, s -> {
                try {
                    return new Result.Ok<>(HairColor.valueOf(s));
                } catch (IllegalArgumentException e) {
                    return new Result.Err<>("Incorrect value");
                }
            }).andThen(this::setHairColor);
            if (res.isErr()) return res;
        }

        {
            var res = nlip.collectInteractively(isInteractive, spacingLevel, String.format("Person's nationality (%s%s, not null)", Country.class.getSimpleName(), Arrays.asList(Country.values())), false, s -> {
                try {
                    return new Result.Ok<>(Country.valueOf(s));
                } catch (IllegalArgumentException e) {
                    return new Result.Err<>("Incorrect value");
                }
            }).andThen(this::setNationality);
            if (res.isErr()) return res;
        }

        {
            if (isInteractive)
                System.out.printf("%sEnter Person's Location (can be null: if you cancel sub-operation via [^D]):%n", " ".repeat(2 * spacingLevel));
            Location location = new Location();
            var res = location.fillFromReader(isInteractive, reader, spacingLevel + 1);
            if (res.isErr()) {
                if (!res.getErr().equals("Operation cancelled"))
                    return new Result.Err<>(res.getErr());
                else
                    System.out.println();
            } else {
                this.location = location;
            }
        }

        return new Result.Ok<>();
    }

    /**
     * Finds difference between objects. ATTENTION: Since object has complex fields which are *basically* (in a logical way) primitives, difference in fields of these complex fields will not be calculated separately.
     * @param targetPerson Person to compare fields with.
     * @return Set of lines with '+'/'-' at beginning of them. ('+': we need to add field with value to this object, '-': 'we need to remove field with value).
     */
    public String diff(Person targetPerson) {
        StringBuilder sb = new StringBuilder();
        String[] thisFmt = this.fieldsToFormattedString().split("\n");
        String[] targetFmt = targetPerson.fieldsToFormattedString().split("\n");
        for (int i = 0; i < thisFmt.length; ++i) {
            if (!thisFmt[i].equals(targetFmt[i])) {
                sb.append('-');
                sb.append(thisFmt[i]);
                sb.append('\n');
                sb.append('+');
                sb.append(targetFmt[i]);
                sb.append('\n');
            }
        }
        if (!sb.isEmpty() && sb.charAt(sb.length() - 1) == '\n')
            sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Person o2 = (Person) o;
        return  this.id.equals(o2.id) &&
                this.name.equals(o2.name) &&
                this.coordinates.equals(o2.coordinates) &&
                this.creationDate.equals(o2.creationDate) &&
                this.height == o2.height &&
                this.eyeColor == o2.eyeColor &&
                this.hairColor == o2.hairColor &&
                this.nationality == o2.nationality &&
                (this.location == null && o2.location == null || this.location != null && this.location.equals(o2.location));
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.id.hashCode(), this.name.hashCode(), this.coordinates.hashCode(), this.creationDate.hashCode(), Long.valueOf(this.height).hashCode(), Objects.hashCode(this.eyeColor), Objects.hashCode(this.hairColor), this.nationality.hashCode(), Objects.hashCode(this.location));
    }

    @Override
    public String toString() {
        return String.format("Person { id = %d, name = \"%s\", coordinates = %s, creationDate = %s, height = %d, eyeColor = %s, hairColor = %s, nationality = %s, location = %s }", this.id, this.name, this.coordinates, this.creationDate, this.height, this.eyeColor, this.hairColor, this.nationality, this.location);
    }

    public String fieldsToFormattedString() {
        return String.format("ID: %d%n", this.id) +
                String.format("Name: \"%s\"%n", this.name) +
                String.format("Coordinates: (%d, %f)%n", this.coordinates.getX(), this.coordinates.getY()) +
                String.format("Creation date: %s%n", this.creationDate.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))) +
                String.format("Height: %d%n", this.height) +
                (this.eyeColor == null ? "No eye color provided\n" : String.format("Eye color: %s%n", this.eyeColor.toString())) +
                (this.hairColor == null ? "No hair color provided\n" : String.format("Hair color: %s%n", this.hairColor.toString())) +
                String.format("Nationality: %s%n", this.nationality.toString()) +
                (this.location == null ? "No location provided" : String.format("Location: (%d, %f, %f | \"%s\")", this.location.getX(), this.location.getY(), this.location.getZ(), this.location.getName()));
    }

    @Override
    public Person clone() {
        try {
            Person clone = (Person) super.clone();
            clone.coordinates = this.coordinates.clone();
            if (this.location != null)
                clone.location = this.location.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /**
     * Compares everything except `id` and `creationDate`.
     * @param o2 Object to compare with.
     * @return [-1, 0, -1]: [less, equal, more]
     */
    public int compareIndividualsTo(@NotNull Person o2) {
        int compareResult;

        compareResult = this.name.compareTo(o2.name);
        if (compareResult != 0)
            return compareResult;

        compareResult = this.coordinates.compareTo(o2.coordinates);
        if (compareResult != 0)
            return compareResult;

        compareResult = Long.compare(this.height, o2.height);
        if (compareResult != 0)
            return compareResult;

        {
            int ordinalLeft = this.eyeColor == null ? -1 : this.eyeColor.ordinal();
            int ordinalRight = o2.eyeColor == null ? -1 : o2.eyeColor.ordinal();
            compareResult = Integer.compare(ordinalLeft, ordinalRight);
        }
        if (compareResult != 0)
            return compareResult;

        {
            int ordinalLeft = this.hairColor == null ? -1 : this.hairColor.ordinal();
            int ordinalRight = o2.hairColor == null ? -1 : o2.hairColor.ordinal();
            compareResult = Integer.compare(ordinalLeft, ordinalRight);
        }
        if (compareResult != 0)
            return compareResult;

        compareResult = this.nationality.compareTo(o2.nationality);
        if (compareResult != 0)
            return compareResult;

        if (this.location == o2.location)
            compareResult = 0;
        else if (this.location == null)
            compareResult = -1;
        else if (o2.location == null)
            compareResult = 1;
        else
            compareResult = this.location.compareTo(o2.location);

        return compareResult;
    }

    @Override
    public int compareTo(@NotNull Person o2) {
        int compareResult;

        compareResult = this.compareIndividualsTo(o2);
        if (compareResult != 0)
            return compareResult;

        compareResult = this.id.compareTo(o2.id);
        if (compareResult != 0)
            return compareResult;

        compareResult = this.creationDate.compareTo(o2.creationDate);

        return compareResult;
    }
}
