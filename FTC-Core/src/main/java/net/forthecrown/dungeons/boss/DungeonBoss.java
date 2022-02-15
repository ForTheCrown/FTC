package net.forthecrown.dungeons.boss;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Worlds;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.dungeons.boss.components.BossComponent;
import net.forthecrown.utils.Nameable;
import net.forthecrown.utils.transformation.FtcBoundingBox;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * A very basic and generic interface representing a
 * Dungeon Boss
 */
public interface DungeonBoss extends Nameable, Listener {
    /**
     * Gets the items required to spawn it
     * @return The items required to spawn this boss
     */
    ImmutableList<ItemStack> getSpawningItems();

    /**
     * Gets whether the boss is alive or not
     * @return Isn't it obvious
     */
    boolean isAlive();

    /**
     * Gets all the components this boss has
     * @return Component set, null, if there are no components
     */
    @Nullable Set<BossComponent> getComponents();

    /**
     * Gets a specific component in this boss' component
     * list
     *
     * @param clazz The component's class
     * @param <T> The type of component
     * @return The component, null, if there are no components
     *         or if the component wasn't found
     */
    <T extends BossComponent> T getComponent(Class<T> clazz);

    /**
     * Spawns the boss, will not check for items, just spawns it
     */
    void spawn();

    /**
     * Kills the boss without forcing it
     */
    default void kill() {
        kill(false);
    }

    /**
     * Kills the boss
     * @param force Whether to force its death, true for
     *              stuff like server restarts
     */
    void kill(boolean force);

    /**
     * Gets the current battle's context
     * @return Current battle's context,
     *         null, if the boss isn't alive
     */
    BossContext currentContext();

    /**
     * Generic getter function, will return
     * the world_void in most instances
     *
     * @return The world this boss is in
     */
    default @NotNull World getWorld() {
        return Worlds.voidWorld();
    }

    /**
     * Gets the bounding box of the room
     * the boss is in
     * @return The boss' room's bounds
     */
    FtcBoundingBox getRoom();

    /**
     * Attempts to spawn the boss
     * @param player The player spawning
     * @return True, if the player had all the items
     *         required to spawn the boss, false otherwise
     */
    default boolean attemptSpawn(Player player) {
        PlayerInventory inventory = player.getInventory();
        List<ItemStack> items = new ObjectArrayList<>();

        for (ItemStack i: getSpawningItems()) {
            // Check if they have the item, if not
            // display item missing message
            if(!inventory.containsAtLeast(i, i.getAmount())) {
                player.sendMessage(
                        Component.translatable("dungeons.notEnoughItems")
                                .color(NamedTextColor.GRAY)
                                .append(Component.newline())
                                .append(itemMessage(inventory))
                );
                return false;
            }

            // I do not trust non cloned item stacks
            items.add(i.clone());
        }

        // Remove all items
        inventory.removeItemAnySlot(items.toArray(ItemStack[]::new));

        spawn();
        return true;
    }

    /**
     * Displays the items needed to spawn
     *
     * @param inventory May be null, this parameter
     *                  is here to allow for the messsage
     *                  to display a darker or brighter
     *                  color for each item, depending on
     *                  if you have said item or not
     *
     * @return The item requirement message
     */
    default Component itemMessage(@Nullable Inventory inventory) {
        TextComponent.Builder text = Component.text()
                .color(NamedTextColor.AQUA)
                .append(Component.translatable("dungeons.neededItems"));

        for (ItemStack i: getSpawningItems()) {
            TextColor color = inventory == null || inventory.containsAtLeast(i, i.getAmount()) ?
                    NamedTextColor.DARK_AQUA : TextColor.color(0, 117, 117);

            text.append(Component.newline())
                    .append(Component.text("- "))
                    .append(FtcFormatter.itemAndAmount(i).color(color));
        }

        return text.build();
    }
}
