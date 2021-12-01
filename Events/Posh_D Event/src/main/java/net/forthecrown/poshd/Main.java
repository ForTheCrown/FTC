package net.forthecrown.poshd;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.crown.EventTimer;
import net.forthecrown.poshd.command.CommandCheckPoint;
import net.forthecrown.poshd.command.CommandStartTimer;
import net.forthecrown.poshd.command.CommandStopTimer;
import net.forthecrown.royalgrenadier.RoyalGrenadier;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Score;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class Main extends JavaPlugin {
    public static final Map<UUID, EventTimer> TIMERS = new Object2ObjectOpenHashMap<>();
    public static Main inst;
    public static Logger logger;

    @Override
    public void onEnable() {
        inst = this;
        logger = getLogger();
        RoyalGrenadier.initialize();

        saveResource("messages.properties", false);
        Messages.load(this);

        new CommandStartTimer();
        new CommandStopTimer();
        new CommandCheckPoint();
    }

    @Override
    public void onDisable() {
    }

    public static EventTimer createTimer(Player player, Consumer<Player> playerConsumer) {
        EventTimer result = new EventTimer(player, Messages.timerFormatter(), playerConsumer);
        TIMERS.put(player.getUniqueId(), result);

        return result;
    }

    public static boolean isBetterScore(Score record, long score) {
        if(!record.isScoreSet()) return true;
        int recordInt = record.getScore();

        return recordInt > score;
    }
}
