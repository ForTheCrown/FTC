package net.forthecrown.dungeons.rewrite_4;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.Crown;
import net.forthecrown.registry.Registries;

import java.util.Map;

public final class BossTypes {
    private BossTypes() {}

    static final Map<BossIdentifier, DungeonBoss> ID_LOOKUP = new Object2ObjectOpenHashMap<>();

    public static void init() {

        Crown.logger().info("BossTypes initialized");
    }

    private static <T extends BossType> T register(T val) {
        return (T) Registries.BOSS_TYPES.register(val.key(), val);
    }
}