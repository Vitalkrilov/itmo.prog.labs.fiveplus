package vitalkrilov.itmo.prog.labs.fiveplus.utilities;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

//TODO: use it everywhere
/**
 * Class required for supporting more than one language.
 */
public class Messages {
    private static final String BUNDLE_NAME = "vitalkrilov.itmo.prog.labs.fiveplus.messages";

    private static ResourceBundle RESOURCE_BUNDLE = null;

    private Messages() {}

    /**
     * Get translated message.
     * @param key Key for specific message.
     * @return Translated message according to current language.
     */
    public static String getString(String key) {
        try {
            if (RESOURCE_BUNDLE == null)
                RESOURCE_BUNDLE = ResourceBundle.getBundle(Messages.BUNDLE_NAME);
            return Messages.RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
