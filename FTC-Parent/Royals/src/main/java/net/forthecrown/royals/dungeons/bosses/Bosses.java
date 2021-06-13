package net.forthecrown.royals.dungeons.bosses;

import net.forthecrown.royals.Royals;
import net.forthecrown.royals.dungeons.bosses.mobs.*;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Spider;
import org.bukkit.entity.WitherSkeleton;

import java.util.HashMap;
import java.util.Map;

public class Bosses {

    private final Royals plugin;
    public static final NamespacedKey key = new NamespacedKey(Royals.inst, "bossitem");
    public static final Map<String, DungeonBoss<?>> BY_NAME = new HashMap<>();

    private static Zhambie zhambie;
    private static Skalatan skalatan;
    private static HideySpidey hideySpidey;
    private static Drawned drawned;

    public Bosses(Royals plugin){
        this.plugin = plugin;
    }

    public static Bosses init(){
        Bosses bosses = new Bosses(Royals.inst);
        bosses.initBosses();

        return bosses;
    }

    public void initBosses(){
        zhambie = new Zhambie(plugin);
        skalatan = new Skalatan(plugin);
        hideySpidey = new HideySpidey(plugin);
        drawned = new Drawned(plugin);
    }

    public void killAllBosses() {
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
