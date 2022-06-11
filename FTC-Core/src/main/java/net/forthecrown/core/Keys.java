package net.forthecrown.core;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.types.KeyArgument;
import org.bukkit.NamespacedKey;

/**
 * Utility methods relating to {@link NamespacedKey}s and {@link net.kyori.adventure.key.Key}s
 */
public interface Keys {
    KeyArgument FTC_KEY_PARSER = KeyArgument.key(Crown.inst());

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
        return key(Crown.inst().namespace(), value);
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
            throw new IllegalStateException("Could not parse %s to key".formatted(reader.getString()));
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
}