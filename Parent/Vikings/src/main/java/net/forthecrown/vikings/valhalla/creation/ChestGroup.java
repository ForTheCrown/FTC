package net.forthecrown.vikings.valhalla.creation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.core.serialization.JsonSerializable;
import org.bukkit.Location;

import java.util.List;

public class ChestGroup implements JsonSerializable {

    public byte maxChests;
    public List<Location> locations;

    public ChestGroup(JsonElement json){
        JsonObject jObject = json.getAsJsonObject();
        this.maxChests = jObject.getAsJsonPrimitive("max_chests").getAsByte();

    }

    public ChestGroup(List<Location> locations, byte max_chests){
        this.maxChests = max_chests;
        this.locations = locations;
    }

    @Override
    public JsonElement serialize() {
        return null;
    }

    public byte getMaxChests() {
        return maxChests;
    }

    public List<Location> getLocations() {
        return locations;
    }
}
