package net.forthecrown.core.api;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public interface ShopStock {
    boolean contains(Material material);

    boolean contains(Material material, int amount);

    boolean containsExampleItem();

    void removeExampleItemAmount();

    void add(@Nonnull ItemStack stack);

    void removeItem(Material material, int amount);

    List<ItemStack> getContents();
    void setContents(@Nonnull List<ItemStack> contents);

    ItemStack getExampleItem();

    void setExampleItem(ItemStack exampleItem);
}
