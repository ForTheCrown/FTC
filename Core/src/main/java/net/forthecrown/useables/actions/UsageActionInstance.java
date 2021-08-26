package net.forthecrown.useables.actions;

import net.forthecrown.useables.UsageTypeInstance;
import org.bukkit.entity.Player;

/**
 * An instance of a UsageAction
 */
public interface UsageActionInstance extends UsageTypeInstance {
    /**
     * The code to execute when a player interacts with this action
     * @param player The interacting player
     */
    void onInteract(Player player);
}
