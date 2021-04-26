package net.forthecrown.vikings.valhalla;

import com.google.gson.JsonObject;
import net.forthecrown.core.api.Nameable;
import net.forthecrown.core.serialization.JsonSerializable;
import net.forthecrown.vikings.Vikings;
import net.forthecrown.vikings.valhalla.generation.RaidAreaCreator;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;

import static net.forthecrown.core.utils.JsonUtils.deserializeLocation;
import static net.forthecrown.core.utils.JsonUtils.serializeLocation;

public class VikingRaid implements Nameable, JsonSerializable {

    private final String name;
    private final Location location;
    private final RaidAreaCreator generator;
    private final Vikings main;

    private boolean active;
    public RaidParty currentParty;
    private RaidListener listener;

    //deserializes
    public VikingRaid(Vikings main, JsonObject json){
        this.main = main;

        name = json.get("name").getAsString();
        location = deserializeLocation(json.get("location").getAsJsonObject());

        generator = new RaidAreaCreator(this, json.get("generator").getAsJsonObject());
    }

    //creates
    public VikingRaid(String name, Location location, Vikings main){
        this.location = location;
        this.name = name;
        this.main = main;

        this.generator = new RaidAreaCreator(this);
    }

    public void init(RaidParty party){
        if(active) return;
        active = true;
        currentParty = party;

        //Make raid area
        generator.create();

        //tp players in
        party.forEach(plr -> plr.teleport(location));

        //Register raid listener
        listener = new RaidListener(this, party, main);
        main.getServer().getPluginManager().registerEvents(listener, main);
    }

    public void end(EndCause cause){
        if(!active) return;
        active = false;

        HandlerList.unregisterAll(listener);
        listener = null;
    }

    @Override
    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public RaidAreaCreator getGenerator() {
        return generator;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public JsonObject serialize(){
        JsonObject result = new JsonObject();

        result.addProperty("name", name);
        result.add("location", serializeLocation(getLocation()));
        result.add("generator", getGenerator().serialize());

        return result;
    }

    public static enum EndCause{
        PLUGIN,
        LOSS,
        SUCCESS
    }
}
