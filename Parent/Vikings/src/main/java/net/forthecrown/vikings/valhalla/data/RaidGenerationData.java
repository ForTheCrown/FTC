package net.forthecrown.vikings.valhalla.data;

import net.forthecrown.vikings.valhalla.VikingRaid;

public class RaidGenerationData {

    public final VikingRaid raid;

    public LootData lootData;
    public WorldData worldData;
    public MobData mobData;
    public TriggerData triggerData;

    public RaidGenerationData(VikingRaid raid) {
        this.raid = raid;
    }

    public LootData getLootData() {
        if(lootData == null) return lootData = new LootData();
        return lootData;
    }

    public WorldData getWorldData() {
        if(worldData == null) return worldData = new WorldData();
        return worldData;
    }

    public MobData getMobData() {
        if(mobData == null) return mobData = new MobData();
        return mobData;
    }

    public TriggerData getTriggerData(){
        if(triggerData == null) return triggerData = new TriggerData();
        return triggerData;
    }
}
