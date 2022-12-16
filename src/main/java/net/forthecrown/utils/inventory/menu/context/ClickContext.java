package net.forthecrown.utils.inventory.menu.context;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.utils.inventory.menu.MenuInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * A context around an inventory click
 * <p>
 * Also allows you to set whether the inventory should be reset
 * with {@link ClickContext#shouldReloadMenu(boolean)} (boolean)}
 * <p>
 * As well as modifying the cooldown after a click with
 * {@link ClickContext#setCooldownTime(int)}. Set to 0 or less
 * for no cooldown.
 */
@Getter
public class ClickContext {
    /**
     * Default cooldown length, 10 ticks
     */
    public static final byte DEFAULT_COOLDOWN = 10;

    /** The inventory being clicked */
    private final MenuInventory inventory;

    /** The player clicking in the inventory */
    private final Player player;

    /** The slot being clicked */
    private final int slot;

    /** The item stack which is current on the cursor */
    private final ItemStack cursorItem;

    /** The type of click */
    private final ClickType clickType;

    /**
     * Determines if the inventory should be reloaded
     * after the click code is executed
     */
    @Accessors(fluent = true)
    @Setter private boolean shouldReloadMenu;

    /**
     * Determines if the vanilla click event should
     * be cancelled
     */
    @Accessors(fluent = true)
    @Setter private boolean cancelEvent;

    /**
     * Determines if the inventory should be closed
     * after this click code is ran
     */
    @Accessors(fluent = true)
    @Setter private boolean shouldClose;

    /**
     * The amount of ticks the user be in menu cooldown,
     * 0 or less to disable
     */
    @Setter private int cooldownTime;

    public ClickContext(MenuInventory inventory, Player player, int slot, ItemStack cursorItem, ClickType type) {
        this.inventory = inventory;
        this.player = player;
        this.slot = slot;
        this.cursorItem = cursorItem;
        this.clickType = type;

        this.cancelEvent = true;
        this.cooldownTime = DEFAULT_COOLDOWN;
    }

    /**
     * Tests if this context should place the player in
     * cooldown
     * @return True, if {@link #getCooldownTime()} is larger than 0, false otherwise
     */
    public boolean shouldCooldown() {
        return cooldownTime > 0;
    }
}