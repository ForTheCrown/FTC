package net.forthecrown.inventory.builder;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class ClickContext {

    private final Player player;
    private final int slot;
    private final ItemStack cursorItem;
    private final ClickType clickType;

    private boolean shouldReload;

    public ClickContext(Player player, int slot, ItemStack cursorItem, ClickType type) {
        this.player = player;
        this.slot = slot;
        this.cursorItem = cursorItem;
        this.clickType = type;
        this.shouldReload = false;
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
}
