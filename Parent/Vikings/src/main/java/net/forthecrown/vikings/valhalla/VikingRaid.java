package net.forthecrown.vikings.valhalla;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.core.CrownBoundingBox;
import net.forthecrown.core.serialization.JsonSerializable;
import net.forthecrown.vikings.valhalla.creation.RaidGenerator;
import org.bukkit.Location;

import static net.forthecrown.core.utils.JsonUtils.*;

public class VikingRaid implements JsonSerializable {

    private final String name;
    private final Location start_location;
    private final RaidGenerator generator;

    private CrownBoundingBox region;

    public VikingRaid (String name, Location start_location){
        this.name = name;
        this.start_location = start_location;
        this.generator = new RaidGenerator(this);
    }

    public VikingRaid(JsonObject json){
        this.name = json.get("name").getAsString();
        this.start_location = deserializeLocation(json.get("start_location").getAsJsonObject());
        this.generator = new RaidGenerator(json.get("generator"), this);

        JsonElement raid_region = json.get("region");
        if(raid_region != null && !raid_region.isJsonNull()) this.region = deserializeBoundingBox(raid_region.getAsJsonObject());
    }

    public CrownBoundingBox getRegion() {
        return region;
    }

    public String getName() {
        return name;
    }

    public RaidGenerator getGenerator() {
        return generator;
    }

    public Location getStartLocation() {
        return start_location;
    }

    public void setRegion(CrownBoundingBox region) {
        this.region = region;
    }

    @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();

        json.add("name", new JsonPrimitive(name));
        json.add("startLocation", serializeLocation(start_location));
        json.add("generator", generator.serialize());

        if(region != null) json.add("region", serializeBoundingBox(region));

        return json;
    }
}
