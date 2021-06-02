package net.forthecrown.vikings.valhalla;

import net.forthecrown.emperor.utils.CrownBoundingBox;
import net.forthecrown.vikings.valhalla.data.RaidGenerationData;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VikingRaid {

    public final String name;
    private final Location startLocation;
    public final CrownBoundingBox region;

    public RaidGenerationData generatorData;
    public Map<UUID, Integer> scoreMap = new HashMap<>();

    public VikingRaid(String name, Location startLocation, CrownBoundingBox box) {
        this.name = name;
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
