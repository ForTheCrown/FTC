package net.forthecrown.vikings.raids.valhalla;

import net.forthecrown.vikings.Vikings;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class LiteralLootTable extends net.forthecrown.core.utils.LiteralLootTable {
    public LiteralLootTable(NamespacedKey key, ItemStack... items){
        super(key, items);
    }

    public LiteralLootTable(ItemStack... items){
        super(new NamespacedKey(Vikings.getInstance(), "generic_viking"), items);
    }
}
