package net.forthecrown.vikings.valhalla.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.utils.BlockPos;
import net.forthecrown.emperor.utils.CrownUtils;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.List;

public class ChestGrouping {

    public Key lootTableKey;
    public final List<BlockPos> possibleLocations  = new ArrayList<>();
    public byte maxChests;

    public ChestGrouping(Key lootTableKey, byte maxChests) {
        this.lootTableKey = lootTableKey;
        this.maxChests = maxChests;
    }

    public ChestGrouping(JsonElement element) throws CommandSyntaxException {
        JsonObject json = element.getAsJsonObject();

        lootTableKey = CrownUtils.parseKey(json.get("lootTable").getAsString());
        maxChests = json.get("maxChests").getAsByte();

        if(!json.has("possibleLocations")) return;

        JsonArray array = json.getAsJsonArray("locations");

        for (JsonElement e: array){
            possibleLocations.add(BlockPos.of(e));
        }
    }
}
