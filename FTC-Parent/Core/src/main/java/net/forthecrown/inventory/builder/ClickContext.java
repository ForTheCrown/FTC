package net.forthecrown.inventory.builder;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ClickContext {

    private final Player player;
    private final int slot;
    private final ItemStack cursorItem;

    public ClickContext(Player player, int slot, ItemStack cursorItem) {
        this.player = player;
        this.slot = slot;
        this.cursorItem = cursorItem;
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
}
