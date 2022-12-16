package net.forthecrown.utils.inventory;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DefaultItemBuilder extends BaseItemBuilder<DefaultItemBuilder> {
    DefaultItemBuilder(Material material, int amount) {
        super(material, amount);
    }

    DefaultItemBuilder(ItemStack stack, ItemMeta baseMeta) {
        super(stack, baseMeta);
    }

    @Override
    protected DefaultItemBuilder getThis() {
        return this;
    }
}