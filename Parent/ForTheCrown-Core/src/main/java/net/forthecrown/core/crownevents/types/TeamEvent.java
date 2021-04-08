package net.forthecrown.core.crownevents.types;

import net.forthecrown.core.crownevents.entries.TeamEntry;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * An event which accepts a team entry
 */
public interface TeamEvent extends CrownEvent<TeamEntry> {
    default void start(Collection<Player> players) {}
    default void start(Player player) {}
    default void start() {}
}
