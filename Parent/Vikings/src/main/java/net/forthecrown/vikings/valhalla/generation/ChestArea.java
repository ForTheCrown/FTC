package net.forthecrown.vikings.valhalla.generation;

import com.google.gson.JsonObject;
import net.forthecrown.core.serialization.JsonSerializable;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class ChestArea implements JsonSerializable {

    private byte maxChests;
    private Collection<Location> possibleLocations;

    public ChestArea(JsonObject json){

    }

    public ChestArea(byte maxChests, Collection<Location> possibleLocations){
        if(possibleLocations.size() < maxChests) throw new IllegalArgumentException("maxChests cannot be larger than possible locations");

        this.possibleLocations = possibleLocations;
        this.maxChests = maxChests;
    }

    public List<Location> getRandomLocs(Random random){
        List<Location> result = new ArrayList<>();
        short safeGuard = 300;

        while (result.size() < maxChests){
            for (Location l: possibleLocations) if(random.nextInt(4) > 0) result.add(l);

            safeGuard--;
            if(safeGuard < 0) throw new IllegalStateException("Endless loop :(");
        }

        return result;
    }

    public byte getMaxChests() {
        return maxChests;
    }

    public void setMaxChests(byte maxChests) {
        this.maxChests = maxChests;
    }

    public Collection<Location> getPossibleLocations() {
        return possibleLocations;
    }

    public void setPossibleLocations(Collection<Location> possibleLocations) {
        this.possibleLocations = possibleLocations;
    }

    @Override
    public JsonObject serialize() {
        return null;
    }
}
