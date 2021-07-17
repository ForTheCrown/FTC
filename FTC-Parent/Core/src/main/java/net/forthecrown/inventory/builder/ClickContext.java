package net.forthecrown.inventory.builder;

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

    private final Player player;
    private final int slot;
    private final ItemStack cursorItem;
    private final ClickType clickType;

    private boolean shouldReload;
    private boolean shouldCancelEvent;
    private int cooldownTime;

    public ClickContext(Player player, int slot, ItemStack cursorItem, ClickType type) {
        this.player = player;
        this.slot = slot;
        this.cursorItem = cursorItem;
        this.clickType = type;

        this.shouldCancelEvent = true;
        this.shouldReload = false;
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

    public void setShouldCancelEvent(boolean shouldCancelEvent) {
        this.shouldCancelEvent = shouldCancelEvent;
    }

    public boolean shouldCancelEvent() {
        return shouldCancelEvent;
    }
}
