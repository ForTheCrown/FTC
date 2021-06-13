package net.forthecrown.vikings.valhalla.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.adventure.PaperAdventure;
import net.forthecrown.core.utils.BlockPos;
import net.forthecrown.core.utils.CrownRandom;
import net.forthecrown.core.utils.loot.WeightedLootTable;
import net.forthecrown.vikings.valhalla.builder.BattleBuilder;
import net.forthecrown.vikings.valhalla.active.RaidParty;
import net.minecraft.server.v1_16_R3.LootTableRegistry;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_16_R3.CraftLootTable;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootData implements RaidData {

    public final List<ChestGrouping> chestGroups = new ArrayList<>();
    public final Map<BlockPos, WeightedLootTable> definiteChests = new HashMap<>();

    public LootData() {
    }

    public LootData(JsonElement element){
        JsonObject json = element.getAsJsonObject();

        JsonElement chestGroupElement = json.get("chestGroups");
        if(chestGroupElement != null && chestGroupElement.isJsonArray()){
            for (JsonElement e: chestGroupElement.getAsJsonArray()){
                try {
                    chestGroups.add(new ChestGrouping(e));
                } catch (CommandSyntaxException ignored) {}
            }
        }

        JsonElement definiteElement = json.get("definiteChests");
        if(definiteElement != null && definiteElement.isJsonArray()){
            for (JsonElement e: definiteElement.getAsJsonArray()){
                JsonObject entry = e.getAsJsonObject();

                BlockPos pos = BlockPos.of(entry.get("pos"));
                WeightedLootTable table = WeightedLootTable.deserialize(entry.get("lootTable"));

                definiteChests.put(pos, table);
            }
        }
    }

    public boolean hasNothingToPlace(){
        return chestGroups.isEmpty() && definiteChests.isEmpty();
    }

    @Override
    public void create(RaidParty party, BattleBuilder generator) {
        if(hasNothingToPlace()) return;

        World world = generator.world;
        Map<BlockPos, LootTable> blocks = new HashMap<>();
        blocks.putAll(definiteChests);

        if(!chestGroups.isEmpty()){
            CrownRandom random = generator.random;

            for (ChestGrouping g: chestGroups){
                List<BlockPos> positions = random.pickRandomEntries(g.possibleLocations, g.maxChests);

                LootTableRegistry r = ((CraftServer) Bukkit.getServer()).getServer().getLootTableRegistry();
                MinecraftKey key = PaperAdventure.asVanilla(g.lootTableKey);

                positions.forEach(p -> blocks.put(p, new CraftLootTable(new NamespacedKey(key.getNamespace(), key.getKey()), r.getLootTable(key))));
            }
        }

        if(!blocks.isEmpty()) {
            for (Map.Entry<BlockPos, LootTable> e: blocks.entrySet()){
                BlockPos pos = e.getKey();
                LootTable table = e.getValue();

                Block block = pos.getBlock(world);
                block.setType(Material.CHEST);

                Chest chest = (Chest) block.getState();

                chest.setLootTable(table);
                table.fillInventory(chest.getBlockInventory(), generator.random, new LootContext.Builder(chest.getLocation()).build());
            }
        }
    }

    @Override
    public JsonObject serialize() {
        if(hasNothingToPlace()) return null;

        JsonObject json = new JsonObject();

        if(!chestGroups.isEmpty()){
            JsonArray groups = new JsonArray();
            chestGroups.forEach(e -> groups.add(e.serialize()));
            json.add("chestGroups", groups);
        }

        if(!definiteChests.isEmpty()){
            JsonArray definite = new JsonArray();
            definiteChests.forEach((p, t) -> {
                JsonObject loot = new JsonObject();

                loot.add("pos", p.serialize());
                loot.add("lootTable", t.serialize());
            });

            json.add("definiteChests", definite);
        }

        return json;
    }
}
