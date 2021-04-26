package net.forthecrown.vikings;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;

public enum VikingLootTable {

    BLACKSMITH (new LiteralLootTable(
            new ItemStack(Material.IRON_AXE),
            new ItemStack(Material.IRON_INGOT, 32)
    )),
    FARM (new LiteralLootTable(
            new ItemStack(Material.WHEAT, 40),
            new ItemStack(Material.IRON_HOE)
    )),
    ARMOURER (new LiteralLootTable(
            new ItemStack(Material.IRON_HELMET),
            new ItemStack(Material.IRON_SWORD),
            new ItemStack(Material.ARROW, 17),
            new ItemStack(Material.BOW)
    ));


    private final LiteralLootTable lootTable;
    VikingLootTable(LiteralLootTable lootTable){
        this.lootTable = lootTable;
    }

    public LootTable getLootTable() {
        return lootTable;
    }
}
