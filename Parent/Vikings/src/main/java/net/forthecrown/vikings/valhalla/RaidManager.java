package net.forthecrown.vikings.valhalla;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.forthecrown.vikings.Vikings;
import net.forthecrown.vikings.valhalla.generation.WorldLoader;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RaidManager {

    //Todo exitLocation is currently a placeholder for the actual exit location
    protected static final Location EXIT_LOCATION = new Location(Bukkit.getWorld("world_void"), -509, 4, -493);
    private static final Map<String, VikingRaid> VIKING_RAIDS = new HashMap<>();
    private static final Map<String, RaidParty> ACTIVE_RAIDS = new HashMap<>();

    private final Vikings vikings;
    private final WorldLoader loader;

    public RaidManager(Vikings vikings){
        this.vikings = vikings;
        this.loader = new WorldLoader(vikings.getServer(), vikings);

        deserializeAll();
    }

    public void serializeAll(){
        for (VikingRaid r: VIKING_RAIDS.values()){
            try {
                File file = new File(vikings.getDataFolder() + File.separator + "raids" + File.separator + r.getName() + ".json");
                if (!file.exists()) file.createNewFile();

                FileWriter fWriter = new FileWriter(file);
                fWriter.write(r.serialize().toString());
            } catch (IOException e){ e.printStackTrace(); }
        }
    }

    public void deserializeAll(){
        File dir = new File(vikings.getDataFolder() + File.separator + "raids");
        if(!dir.exists()) dir.mkdirs();
        else if(!dir.isDirectory()) throw new IllegalStateException("raids file is not directory");

        for (File f: dir.listFiles()){
            try {
                FileReader reader = new FileReader(f);
                JsonParser parser = new JsonParser();

                JsonObject object = parser.parse(reader).getAsJsonObject();
                registerRaid(new VikingRaid(vikings, object));
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public void registerParty(String name, RaidParty party){
        ACTIVE_RAIDS.put(name, party);
    }

    public void removeParty(String s){
        ACTIVE_RAIDS.remove(s);
    }

    public RaidParty partyFromName(String s){
        if(ACTIVE_RAIDS.containsKey(s)) return ACTIVE_RAIDS.get(s);
        return null;
    }

    public VikingRaid fromName(String name){
        if(VIKING_RAIDS.containsKey(name)) return VIKING_RAIDS.get(name);
        return null;
    }

    public void registerRaid(VikingRaid vikingRaid){
        VIKING_RAIDS.put(vikingRaid.getName(), vikingRaid);
    }

    public VikingRaid createRaid(String name, Location location){
        VikingRaid raid = new VikingRaid(name, location, vikings);
        registerRaid(raid);
        return raid;
    }

    public Collection<VikingRaid> getRaids(){
        return VIKING_RAIDS.values();
    }

    public Map<String, VikingRaid> getKnownRaids(){
        return VIKING_RAIDS;
    }

    public void unregisterRaid(VikingRaid vikingRaid){
        VIKING_RAIDS.remove(vikingRaid);
    }

    public Location getExitLocation() {
        return EXIT_LOCATION;
    }

    public void callRaid(RaidParty party){
        party.selectedRaid.init(party);
    }

    public WorldLoader getLoader() {
        return loader;
    }
}
