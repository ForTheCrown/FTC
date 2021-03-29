package net.forthecrown.core.crownevents.types;

import net.forthecrown.core.crownevents.entries.TeamEntry;
import org.bukkit.entity.Player;

import java.util.Collection;

public interface TeamEvent extends CrownEvent<TeamEntry> {
    //Implement none, or one, or both lol
    default void start(Collection<Player> players) {}
    default void start() {}
}
