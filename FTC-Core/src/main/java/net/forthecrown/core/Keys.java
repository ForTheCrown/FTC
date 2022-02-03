package net.forthecrown.core;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.types.KeyArgument;
import org.bukkit.NamespacedKey;

public interface Keys {
    KeyArgument FTC_KEY_PARSER = KeyArgument.key(Crown.inst());

    static NamespacedKey key(String namespace, String value) {
        return new NamespacedKey(namespace, value);
    }

    static NamespacedKey forthecrown(String value){
        return key(Crown.inst().namespace(), value);
    }

    static NamespacedKey minecraft(String name) {
        return NamespacedKey.minecraft(name);
    }

    static NamespacedKey parse(String str) throws IllegalStateException {
        return parse(new StringReader(str));
    }

    static NamespacedKey parse(StringReader reader) throws IllegalStateException {
        try {
            return argumentType().parse(reader);
        } catch (CommandSyntaxException e) {
            throw new IllegalStateException("Could not parse %s to key".formatted(reader.getString()));
        }
    }

    static KeyArgument argumentType() {
        return FTC_KEY_PARSER;
    }
}
