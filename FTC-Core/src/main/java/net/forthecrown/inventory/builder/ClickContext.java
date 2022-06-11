package net.forthecrown.inventory.builder;

import net.forthecrown.inventory.FtcInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * A context around an inventory click
 * <p></p>
 * Also allows you to set whether the inventory should be reset with {@link ClickContext#setReloadInventory(boolean)}
 * <p></p>
 * As well as modifying the cooldown after a click with {@link ClickContext#setCooldownTime(int)}.
 * Set to 0 or less for no cooldown.
 */
public class ClickContext {
    /**
     * Default cooldown length, 5 ticks
     */
    public static final byte DEFAULT_COOLDOWN = 5;

    private final FtcInventory inventory;
    private final Player player;
    private final int slot;
    private final ItemStack cursorItem;
    private final ClickType clickType;

    private boolean shouldReload, shouldCancelEvent, shouldClose;
    private int cooldownTime;

    public ClickContext(FtcInventory inventory, Player player, int slot, ItemStack cursorItem, ClickType type) {
        this.inventory = inventory;
        this.player = player;
        this.slot = slot;
        this.cursorItem = cursorItem;
        this.clickType = type;

        this.shouldCancelEvent = true;
        this.cooldownTime = DEFAULT_COOLDOWN;
    }

    public Player getPlayer() {
        return player;
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getCursorItem() {
        return cursorItem;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public boolean shouldReload() {
        return shouldReload;
    }

    public void setReloadInventory(boolean shouldReloadInventory) {
        this.shouldReload = shouldReloadInventory;
    }

    public boolean shouldCooldown() {
        return cooldownTime > 0;
    }

    public int getCooldownTime() {
        return cooldownTime;
    }

    public void setCooldownTime(int cooldownTime) {
        this.cooldownTime = cooldownTime;
    }

    public void setCancelEvent(boolean shouldCancelEvent) {
        this.shouldCancelEvent = shouldCancelEvent;
    }

    public boolean shouldCancelEvent() {
        return shouldCancelEvent;
    }

    public void setShouldClose(boolean shouldClose) {
        this.shouldClose = shouldClose;
    }

    public boolean shouldClose() {
        return shouldClose;
    }

    public FtcInventory getInventory() {
        return inventory;
    }
}