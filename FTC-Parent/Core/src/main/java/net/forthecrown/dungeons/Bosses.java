package net.forthecrown.dungeons;

import net.forthecrown.dungeons.bosses.*;
import net.forthecrown.squire.Squire;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Spider;
import org.bukkit.entity.WitherSkeleton;

import java.util.HashMap;
import java.util.Map;

public class Bosses {

    public static final NamespacedKey key = Squire.createRoyalKey("bossitem");
    public static final Map<String, DungeonBoss<?>> BY_NAME = new HashMap<>();

    private static Zhambie zhambie;
    private static Skalatan skalatan;
    private static HideySpidey hideySpidey;
    private static Drawned drawned;

    private Bosses(){
    }

    public static void init(){
        zhambie = new Zhambie();
        skalatan = new Skalatan();
        hideySpidey = new HideySpidey();
        drawned = new Drawned();
    }

    public static void shutDown() {
        zhambie.kill(true);
        skalatan.kill(true);
        hideySpidey.kill(true);
        drawned.kill(true);
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
}
