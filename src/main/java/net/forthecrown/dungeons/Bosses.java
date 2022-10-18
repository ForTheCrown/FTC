package net.forthecrown.dungeons;

import net.forthecrown.core.registry.Keys;
import net.forthecrown.dungeons.boss.*;
import net.forthecrown.dungeons.boss.evoker.EvokerBoss;
import net.forthecrown.dungeons.usables.ActionBossInfo;
import net.forthecrown.dungeons.usables.ActionEntranceInfo;
import net.forthecrown.dungeons.usables.ActionGiveArtifact;
import net.forthecrown.dungeons.usables.ActionSpawnBoss;
import org.bukkit.NamespacedKey;

import static net.forthecrown.core.registry.Registries.DUNGEON_BOSSES;
import static net.forthecrown.core.registry.Registries.USAGE_ACTIONS;

public class Bosses {
    private Bosses() {}

    public static final NamespacedKey KEY = Keys.royals("bossitem"); // God, I wish there was an underscore in this
    public static final NamespacedKey BOSS_TAG = Keys.royals("boss_tag");

    public static final EvokerBoss EVOKER = register(new EvokerBoss());

    public static final SimpleBoss
            ZHAMBIE         = register(new ZhambieBoss()),
            SKALATAN        = register(new SkalatanBoss()),
            HIDEY_SPIDEY    = register(new HideySpideyBoss()),
            DRAWNED         = register(new DrawnedBoss());

    static void init() {
        DUNGEON_BOSSES.freeze();

        USAGE_ACTIONS.register("entrance_info", ActionEntranceInfo.TYPE);
        USAGE_ACTIONS.register("give_artifact", ActionGiveArtifact.TYPE);
        USAGE_ACTIONS.register("boss_info", ActionBossInfo.TYPE);
        USAGE_ACTIONS.register("spawn_boss", ActionSpawnBoss.TYPE);
    }

    private static <T extends KeyedBoss> T register(T boss) {
        return (T) DUNGEON_BOSSES.register(boss.getKey(), boss).getValue();
    }

    public static void shutdown() {
        DUNGEON_BOSSES.forEach(boss -> boss.kill(true));
    }
}