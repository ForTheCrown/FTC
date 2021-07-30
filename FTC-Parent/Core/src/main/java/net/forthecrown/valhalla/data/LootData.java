package net.forthecrown.valhalla.data;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.utils.MapUtils;
import net.forthecrown.utils.math.BlockPos;
import net.forthecrown.valhalla.RaidGenerationContext;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.loot.LootTable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.forthecrown.valhalla.RaidUtil.applyLootTable;

public class LootData implements RaidData {

    private final Map<BlockPos, Key> definiteSpawns;
    private final Map<Key, ChestGroup> chestGroups;

    public LootData() {
        this.definiteSpawns = new HashMap<>();
        this.chestGroups = new HashMap<>();
    }

    public LootData(Map<BlockPos, Key> definiteSpawns, Map<Key, ChestGroup> chestGroups) {
        this.definiteSpawns = definiteSpawns;
        this.chestGroups = chestGroups;
    }

    @Override
    public void generate(RaidGenerationContext context) {
        World world = context.getWorld();
        CrownRandom random = context.getRandom();
        float mod = context.getDifficulty().getModifier();

        if(hasDefiniteSpawns()) {
            for (Map.Entry<BlockPos, Key> e: definiteSpawns.entrySet()) {
                BlockPos pos = e.getKey();
                LootTable table = Bukkit.getLootTable(FtcUtils.keyToBukkit(e.getValue()));

                if(table == null) {
                    ForTheCrown.logger().warning("Found key pointing to null loot table in definite chests: " + e.getValue().asString());
                    continue;
                }

                applyLootTable(pos, world, table, random, mod);
            }
        }

        if(!hasChestGroups()) return;

        for (ChestGroup g: chestGroups.values()) {
            LootTable loot = Bukkit.getLootTable(FtcUtils.keyToBukkit(g.getLootTableKey()));

            if(loot == null) {
                ForTheCrown.logger().warning("Found key pointing to null loot table in chest group: " + g.getLootTableKey().asString());
                continue;
            }
            if(ListUtils.isNullOrEmpty(g.getPossibleLocations())) continue;

            List<BlockPos> chosenPositions = random.pickRandomEntries(
                    g.getPossibleLocations(),
                    random.intInRange(1, g.getMax())
            );

            for (BlockPos pos: chosenPositions) applyLootTable(pos, world, loot, random, mod);
        }
    }

    public boolean hasDefiniteSpawns() {
        return !MapUtils.isNullOrEmpty(getDefiniteSpawns());
    }

    public Map<BlockPos, Key> getDefiniteSpawns() {
        return definiteSpawns;
    }

    public void setChest(BlockPos pos, Key lootTable) {
        definiteSpawns.put(pos, FtcUtils.checkNotBukkit(lootTable));
    }

    public boolean hasChestAt(BlockPos pos) {
        return definiteSpawns.containsKey(pos);
    }

    public void removeChest(BlockPos pos) {
        definiteSpawns.remove(pos);
    }

    public Key getLootTable(BlockPos pos) {
        return definiteSpawns.get(pos);
    }

    public boolean hasChestGroups() {
        return !MapUtils.isNullOrEmpty(chestGroups);
    }

    public void addGroup(ChestGroup group) {
        chestGroups.put(group.key(), group);
    }

    public void removeGroup(Key group) {
        chestGroups.remove(FtcUtils.checkNotBukkit(group));
    }

    public boolean hasGroup(Key key) {
        return chestGroups.containsKey(FtcUtils.checkNotBukkit(key));
    }

    public ChestGroup getChestGroup(Key key) {
        return chestGroups.get(FtcUtils.checkNotBukkit(key));
    }

    public Map<Key, ChestGroup> getChestGroups() {
        return chestGroups;
    }
}
