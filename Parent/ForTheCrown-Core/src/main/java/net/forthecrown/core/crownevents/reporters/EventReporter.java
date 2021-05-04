package net.forthecrown.core.crownevents.reporters;

import net.forthecrown.core.api.Announcer;
import net.forthecrown.core.crownevents.types.CrownEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.Closeable;
import java.util.logging.Level;

public interface EventReporter extends Closeable {

    void log(Level level, String info);

    Plugin getPlugin();

    CrownEvent<?> getEvent();

    default void debug(String info){
        log(Announcer.DebugLevel.DEBUG, info);
    }

    default void info(String info){
        log(Level.INFO, info);
    }

    default void warn(String info){
        log(Level.WARNING, info);
    }

    default void severe(String info){
        log(Level.SEVERE, info);
    }

    default void logEntry(Player player){
        logAction(player, "Entered the event");
    }

    default void logExit(Player player, boolean success){
        logAction(player, "Exited the event, completed: " + success);
    }

    default void logExit(Player player, int score){
        logAction(player, "Exited the event, score: " + score);
    }

    default void logExit(Player player, int score, String info){
        logAction(player, "Exited the event, score: " + score + ". extra: " + info);
    }

    default void logExit(Player player){
        logExit(player, false);
    }

    default void logAction(Player player, String info){
        log(Level.INFO, player.getName() + ": " + info);
    }
}
