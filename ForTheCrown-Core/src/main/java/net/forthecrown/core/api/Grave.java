package net.forthecrown.core.api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Grave {
    CrownUser getUser();

    void giveItems();

    boolean isEmpty();

    void addItem(@NotNull ItemStack item);

    void addItem(@NotNull ItemStack... items);

    void setItems(@NotNull List<ItemStack> items);

    List<ItemStack> getItems();
}
