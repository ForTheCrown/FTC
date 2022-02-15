package net.forthecrown.dungeons;

import net.forthecrown.dungeons.boss.*;
import net.forthecrown.dungeons.usables.*;
import net.forthecrown.registry.Registries;
import net.forthecrown.squire.Squire;
import net.forthecrown.useables.actions.UsageAction;
import org.bukkit.NamespacedKey;

public class Bosses {

    public static final NamespacedKey KEY = Squire.createRoyalKey("bossitem"); // God, I wish there was an underscore in this
    public static final NamespacedKey BOSS_TAG = Squire.createRoyalKey("boss_tag");
    public static final DungeonUserDataAccessor ACCESSOR = new DungeonUserDataAccessor();

    public static final SimpleBoss
            ZHAMBIE         = register(new ZhambieBoss()),
            SKALATAN        = register(new SkalatanBoss()),
            HIDEY_SPIDEY    = register(new HideySpideyBoss()),
            DRAWNED         = register(new DrawnedBoss());

    private Bosses(){
    }

    public static void init() {
        Registries.DUNGEON_BOSSES.close();

        register(new ActionGiveArtifact());
        register(new ActionSpawnBoss());
        register(new ActionEntranceInfo());
        register(new ActionShowBossInfo());

        Registries.NPCS.register(DiegoNPC.KEY, new DiegoNPC());
        Registries.USAGE_CHECKS.register(CheckBeatenBoss.KEY, new CheckBeatenBoss());
    }

    private static void register(UsageAction<?> action){
        Registries.USAGE_ACTIONS.register(action.key(), action);
    }

    private static <T extends KeyedBoss> T register(T boss) {
        return (T) Registries.DUNGEON_BOSSES.register(boss.key(), boss);
    }

    public static void shutDown() {
        Registries.DUNGEON_BOSSES.forEach(boss -> boss.kill(true));
    }
}
