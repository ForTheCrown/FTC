package net.forthecrown.vikings.valhalla;

import net.forthecrown.vikings.valhalla.data.RaidGenerationData;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VikingRaid {

    public final Key key;
    private final Location startLocation;
    public final BoundingBox region;

    public RaidGenerationData generatorData;
    public Map<UUID, Integer> scoreMap = new HashMap<>();

    public VikingRaid(Key key, Location startLocation, BoundingBox box) {
        this.key = key;
        this.startLocation = startLocation;
        this.region = box;
    }

    public RaidGenerationData getGeneratorData() {
        if(generatorData == null) return generatorData = new RaidGenerationData(this);
        return generatorData;
    }

    public void setScore(UUID id, Integer score){
        scoreMap.put(id, score);
    }

    public void removeScore(UUID id){
        scoreMap.remove(id);
    }

    public boolean hasScore(UUID id){
        return scoreMap.containsKey(id);
    }

    public Location getStartLocation() {
        return startLocation.clone();
    }
}
