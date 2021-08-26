package net.forthecrown.useables.kits;

import net.forthecrown.utils.Nameable;
import net.forthecrown.utils.SilentPredicate;
import net.forthecrown.serializer.Deletable;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.useables.Preconditionable;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.function.Predicate;

/**
 * A kit, aka a group of items which can be gotten with the /kit command
 */
public interface Kit extends
        JsonSerializable, Preconditionable, Predicate<Player>,
        SilentPredicate<Player>, Nameable, Keyed,
        HoverEventSource<Component>, Deletable
{
    /**
     * Attempts to give the kit to the player
     * @param player The player to give
     * @return Whether the giving was a success or not lol
     */
    boolean attemptItemGiving(Player player);

    /**
     * Gives the items to the player, ignoring inventory and UsageCheck limits
     * @param player The player to give
     */
    void giveItems(Player player);

    /**
     * Gets the display name of this kit
     * @return The kit's display name
     */
    default Component displayName(){
        return Component.text(getName())
                .hoverEvent(this)
                .clickEvent(ClickEvent.runCommand("/kit " + getName()));
    }

    /**
     * Gets the items in this kit
     * @return The kit's items
     */
    List<ItemStack> getItems();

    /**
     * Checks whether the given inventory has space for all the items
     * @param inventory the inventory to check
     * @return Whether the inventory has space for all the kit's items
     */
    boolean hasSpace(PlayerInventory inventory);
}
