package net.forthecrown.crownevents.reporters;

import net.forthecrown.crownevents.CrownEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public interface EventLogger extends Logger {
    Logger getBaseLogger();

    Plugin getPlugin();

    CrownEvent<?> getEvent();

    default void logEntry(Player player){
        logAction(player, "Entered the event");
    }

    default void logExit(Player player) {
        logAction(player, "Exited the event");
    }

    default void logExit(Player player, int score){
        logAction(player, "Exited the event, score: " + score);
    }

    default void logExit(Player player, int score, String info){
        logAction(player, "Exited the event, score: " + score + ". extra: " + info);
    }

    default void logAction(Player player, String info){
        log(Level.INFO, player.getName() + ": " + info);
    }
}
