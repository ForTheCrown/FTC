package net.forthecrown.utils.inventory;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DefaultItemBuilder extends BaseItemBuilder<DefaultItemBuilder> {
    public DefaultItemBuilder(Material material, int amount) {
        super(material, amount);
    }

    @Override
    protected DefaultItemBuilder getThis() {
        return this;
    }

    @Override
    protected void onBuild(ItemStack item, ItemMeta meta) {
    }
}