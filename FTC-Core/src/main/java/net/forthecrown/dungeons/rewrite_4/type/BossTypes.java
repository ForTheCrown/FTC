package net.forthecrown.dungeons.rewrite_4.type;

import net.forthecrown.core.Crown;
import net.forthecrown.registry.Registries;

public final class BossTypes {
    private BossTypes() {}

    public static long
        ID_ZHAMBIE = 1L,
        ID_SKALATAN = 2L,
        ID_SPIDEY = 3L,
        ID_DRAWNED = 4L,
        ID_EMO = 5L;

    public static void init() {

        Crown.logger().info("BossTypes initialized");
    }

    private static <T extends BossType> T register(T val) {
        return (T) Registries.BOSS_TYPES.register(val.key(), val);
    }
}