package net.forthecrown.vikings.valhalla.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.serializer.JsonSerializable;
import net.forthecrown.core.utils.BlockPos;
import net.forthecrown.core.utils.CrownUtils;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.List;

public class ChestGrouping implements JsonSerializable {

    public final Key lootTableKey;
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

        if(!json.has("locations")) return;

        JsonArray array = json.getAsJsonArray("locations");

        for (JsonElement e: array){
            possibleLocations.add(BlockPos.of(e));
        }
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();

        json.add("lootTable", new JsonPrimitive(lootTableKey.asString()));
        json.add("maxChests", new JsonPrimitive(maxChests));

        if(possibleLocations.size() > 0){
            JsonArray array = new JsonArray();

            possibleLocations.forEach(pos -> array.add(pos.serialize()));
            json.add("locations", array);
        }

        return json;
    }
}
