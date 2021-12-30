package net.forthecrown.dungeons;

import net.forthecrown.dungeons.bosses.*;
import net.forthecrown.dungeons.usables.*;
import net.forthecrown.registry.Registries;
import net.forthecrown.squire.Squire;
import net.forthecrown.useables.actions.SimpleAction;
import net.forthecrown.useables.actions.UsageAction;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Spider;
import org.bukkit.entity.WitherSkeleton;

public class Bosses {

    public static final NamespacedKey KEY = Squire.createRoyalKey("bossitem");
    public static final NamespacedKey BOSS_TAG = Squire.createRoyalKey("bosstag");
    private static final DungeonUserDataAccessor accessor = new DungeonUserDataAccessor();

    private static Zhambie zhambie;
    private static Skalatan skalatan;
    private static HideySpidey hideySpidey;
    private static Drawned drawned;

    private Bosses(){
    }

    public static void init(){
        zhambie = register(new Zhambie());
        skalatan = register(new Skalatan());
        hideySpidey = register(new HideySpidey());
        drawned = register(new Drawned());

        Registries.DUNGEON_BOSSES.close();

        register(new ActionGiveArtifact());
        register(new ActionSpawnBoss());
        register(new SimpleAction<>(ActionDiego.KEY, ActionDiego::new));
        register(new ActionEntranceInfo());
        register(new ActionShowBossInfo());

        Registries.USAGE_CHECKS.register(CheckBeatenBoss.KEY, new CheckBeatenBoss());
    }

    private static void register(UsageAction<?> action){
        Registries.USAGE_ACTIONS.register(action.key(), action);
    }

    private static <T extends DungeonBoss> T register(T boss) {
        return (T) Registries.DUNGEON_BOSSES.register(boss.key(), boss);
    }

    public static void shutDown() {
        Registries.DUNGEON_BOSSES.forEach(boss -> boss.kill(true));
    }

    public static DungeonBoss<Husk> zhambie() {
        return zhambie;
    }

    public static DungeonBoss<WitherSkeleton> skalatan() {
        return skalatan;
    }

    public static DungeonBoss<Spider> hideySpidey() {
        return hideySpidey;
    }

    public static DungeonBoss<Drowned> drawned() {
        return drawned;
    }

    public static DungeonUserDataAccessor getAccessor() {
        return accessor;
    }
}
