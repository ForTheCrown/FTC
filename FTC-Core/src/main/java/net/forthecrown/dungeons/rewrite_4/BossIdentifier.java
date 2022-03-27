package net.forthecrown.dungeons.rewrite_4;

import net.forthecrown.core.Keys;
import net.forthecrown.dungeons.rewrite_4.type.BossType;
import net.forthecrown.registry.Registries;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.Validate;

import java.util.Random;

public record BossIdentifier(BossType type, long id) {
    private static final Random RANDOM = new Random();

    public static BossIdentifier create(BossType type) {
        return new BossIdentifier(type, RANDOM.nextLong());
    }

    @Override
    public String toString() {
        return type.toString() + ":" + id;
    }

    public static BossIdentifier parse(String input) {
        int lastColon = input.lastIndexOf(':');
        Validate.isTrue(lastColon != -1, "Invalid input: '%s'", input);

        String before = input.substring(0, lastColon);
        String after = input.substring(lastColon + 1);

        Key key = Keys.parse(before);
        long id = Long.parseLong(after);
        BossType type = Registries.BOSS_TYPES.get(key);

        return new BossIdentifier(Validate.notNull(type, "unknown boss type: '%s'", key), id);
    }
}