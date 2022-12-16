package net.forthecrown.core.registry;

import lombok.Getter;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.Validate;

@Getter
public class RegistryKey {
    private static final String DELIMITER = "::";

    private final String registry;
    private final String value;

    private RegistryKey(String registry, String value) {
        this.registry = Keys.ensureValid(registry);
        this.value = Keys.ensureValid(value);
    }

    public static RegistryKey of(String registry, String name) {
        return new RegistryKey(registry, name);
    }

    public static RegistryKey of(String s) {
        String[] split = s.split(DELIMITER);
        Validate.isTrue(split.length == 2, "Invalid registry key: '%s'", s);

        return of(split[0], split[1]);
    }

    public static String toString(String registry, String value) {
        return registry + DELIMITER + value;
    }

    public static RegistryKey load(Tag t) {
        if (!(t instanceof StringTag stringTag)) {
            return null;
        }

        return of(stringTag.getAsString());
    }

    public StringTag save() {
        return StringTag.valueOf(toString());
    }

    @Override
    public String toString() {
        return toString(registry, value);
    }
}