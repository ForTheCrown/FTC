package net.forthecrown.valhalla.data;

import net.forthecrown.core.CrownCore;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.MapUtils;
import net.forthecrown.utils.math.BlockPos;
import net.forthecrown.valhalla.RaidGenerationContext;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;

import java.util.List;
import java.util.Map;

public class LootData implements RaidData {

    private Map<BlockPos, Key> definiteSpawns;
    private Map<Key, ChestGroup> chestGroups;

    public LootData() {
    }

    public LootData(Map<BlockPos, Key> definiteSpawns, Map<Key, ChestGroup> chestGroups) {
        this.definiteSpawns = definiteSpawns;
        this.chestGroups = chestGroups;
    }

    @Override
    public void generate(RaidGenerationContext context) {
        World world = context.getWorld();
        CrownRandom random = context.getRandom();

        for (Map.Entry<BlockPos, Key> e: definiteSpawns.entrySet()) {
            BlockPos pos = e.getKey();
            LootTable table = Bukkit.getLootTable(FtcUtils.toBukkit(e.getValue()));

            if(table == null) {
                CrownCore.logger().warning("Found key pointing to null loot table in definite chests: " + e.getValue().asString());
                continue;
            }

            Block block = pos.getBlock(world);
            block.setType(Material.CHEST);

            Chest chest = (Chest) block.getState();
            chest.setLootTable(table);
            table.fillInventory(
                    chest.getBlockInventory(),
                    random,
                    new LootContext.Builder(chest.getLocation()).build()
            );
        }

        for (ChestGroup g: chestGroups.values()) {
            LootTable loot = Bukkit.getLootTable(FtcUtils.toBukkit(g.getLootTableKey()));

            if(loot == null) {
                CrownCore.logger().warning("Found key pointing to null loot table in chest group: " + g.getLootTableKey().asString());
                continue;
            }
            if(g.getPossibleLocations() == null) continue;

            List<BlockPos> chosenPositions = random.pickRandomEntries(
                    g.getPossibleLocations(),
                    random.intInRange(1, g.getMaxChests())
            );

            for (BlockPos pos: chosenPositions) {
                Block block = pos.getBlock(world);
                block.setType(Material.CHEST);

                Chest chest = (Chest) block.getState();

                chest.setLootTable(loot);
                loot.fillInventory(
                        chest.getBlockInventory(),
                        random,
                        new LootContext.Builder(chest.getLocation()).build()
                );
            }
        }
    }

    public boolean hasDefiniteSpawns() {
        return !MapUtils.isNullOrEmpty(getDefiniteSpawns());
    }

    public Map<BlockPos, Key> getDefiniteSpawns() {
        return definiteSpawns;
    }

    public void setDefiniteSpawns(Map<BlockPos, Key> definiteSpawns) {
        this.definiteSpawns = definiteSpawns;
    }

    public void addChest(BlockPos pos, Key lootTable) {
        definiteSpawns.put(pos, lootTable);
    }

    public void removeChest(BlockPos pos) {
        definiteSpawns.remove(pos);
    }

    public boolean hasChestGroups() {
        return !MapUtils.isNullOrEmpty(chestGroups);
    }

    public void addGroup(ChestGroup group) {
        chestGroups.put(group.key(), group);
    }

    public void removeGroup(ChestGroup group) {
        chestGroups.remove(group.key());
    }

    public Map<Key, ChestGroup> getChestGroups() {
        return chestGroups;
    }
}
