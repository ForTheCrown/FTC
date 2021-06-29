package net.forthecrown.cosmetics.custominvs;

import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CustomInv implements InventoryHolder {

    private final Inventory inv;
    private final Border invBorder;
    private final Map<Integer, Option> invSlots;
    public final static Listener listener = new InvClickListener();

    @Override
    public @NotNull Inventory getInventory() {
        return this.inv;
    }

    public CustomInv(@NotNull int size, TextComponent title, @NotNull Border invBorder, @NotNull Map<Integer, Option> invSlots) {
        this.invSlots = invSlots;
        this.invBorder = invBorder;

        this.inv = createInv(size, title);
    }

    private Inventory createInv(int size, TextComponent title) {
        Inventory result;
        if (title == null) result = Bukkit.createInventory(this, size);
        else result = Bukkit.createInventory(this, size, title);

        for (Map.Entry<Integer, Option> optionSlot : invSlots.entrySet())
            result.setItem(optionSlot.getKey(), optionSlot.getValue().getItem());

        invBorder.applyBorder(result);
        return result;
    }

    public void handleClick(int slot) {
        if (invBorder.isOnBorder(slot)) invBorder.handleClick();
        else if (invSlots.containsKey(slot)) invSlots.get(slot).handleClick();
    }
}
