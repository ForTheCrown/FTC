package net.forthecrown.core.registry;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.types.KeyArgument;
import org.apache.commons.lang3.Validate;
import org.bukkit.NamespacedKey;
import org.intellij.lang.annotations.Language;

import java.util.regex.Pattern;

/**
 * Utility methods relating to {@link NamespacedKey}s and {@link net.kyori.adventure.key.Key}s
 */
public interface Keys {
    /**
     * The regex for determining if a {@link Registry} key
     * is valid
     */
    @Language("RegExp")
    String VALID_KEY_REGEX = "[a-zA-Z0-9+\\-/._]+";

    /** Pattern made with {@link #VALID_KEY_REGEX} */
    Pattern VALID_KEY = Pattern.compile(VALID_KEY_REGEX);

    /**
     * Namespaced key parser with "forthecrown" as the default
     * namespace
     */
    KeyArgument FTC_KEY_PARSER = KeyArgument.key(Crown.plugin());

    /**
     * Creates a key with the given namespace and value
     * @param namespace The namespace to use
     * @param value The value to use
     * @return The created key
     */
    static NamespacedKey key(String namespace, String value) {
        return new NamespacedKey(namespace, value);
    }

    /**
     * Creates a key with 'forthecrown' as the namespace
     * @param value The key's value
     * @return The created key
     */
    static NamespacedKey forthecrown(String value){
        return key(Crown.plugin().namespace(), value);
    }

    /**
     * Creates a key with 'minecraft' as the namespace
     * @param name The key's value
     * @return The created key
     */
    static NamespacedKey minecraft(String name) {
        return NamespacedKey.minecraft(name);
    }

    /**
     * Parses a given string into a key
     * @param str The key to parse
     * @return The parsed key
     * @throws IllegalStateException If the parsing failed
     */
    static NamespacedKey parse(String str) throws IllegalStateException {
        return parse(new StringReader(str));
    }

    static NamespacedKey royals(String str) {
        return key("royals", str);
    }

    /**
     * Parses a given string reader into a key
     * @param reader The reader to use for parsing
     * @return The parsed key
     * @throws IllegalStateException If the parsing failed
     */
    static NamespacedKey parse(StringReader reader) throws IllegalStateException {
        try {
            return argumentType().parse(reader);
        } catch (CommandSyntaxException e) {
            throw new IllegalStateException("Could not parse '%s' to key".formatted(reader.getString()));
        }
    }

    /**
     * Gets the Key argument type, uses 'forthecrown'
     * as it's default namespace
     * @return The FTC key parser
     */
    static KeyArgument argumentType() {
        return FTC_KEY_PARSER;
    }

    /**
     * Ensures a given key matches the {@link #VALID_KEY_REGEX}
     * regex pattern
     * @param s The string to test
     * @return The input string
     * @throws IllegalArgumentException If the given key was not valid
     */
    static @org.intellij.lang.annotations.Pattern(VALID_KEY_REGEX) String ensureValid(String s)
            throws IllegalArgumentException
    {
        Validate.isTrue(isValidKey(s), "Invalid key: '%s', did not match '%s' pattern", s, VALID_KEY_REGEX);
        return s;
    }

    /**
     * Tests if the entire given string matches the
     * {@link #VALID_KEY_REGEX} regex pattern
     * @param s The string to test
     * @return True, if the entire string matches, false otherwise
     */
    static boolean isValidKey(String s) {
        return VALID_KEY.matcher(s).matches();
    }

    /**
     * Tests if a given character matches the {@link #VALID_KEY_REGEX}
     * regex pattern and is valid
     * @param c The character to test
     * @return True, if the character matches, false otherwise
     */
    static boolean isValidKeyChar(char c) {
        return isValidKey("" + c);
    }
}