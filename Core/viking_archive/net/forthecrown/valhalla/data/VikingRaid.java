package net.forthecrown.valhalla.data;

import net.forthecrown.utils.FtcUtils;
import net.forthecrown.valhalla.data.triggers.TriggerData;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minecraft.core.Position;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public class VikingRaid implements Keyed {

    private final Key key;
    private final BoundingBox region;
    private final Position startingPos;

    private LootData lootData;
    private MobData mobData;
    private TriggerData triggerData;

    public VikingRaid(Key key, BoundingBox region, Position startingPos) {
        this.key = key;
        this.region = region;
        this.startingPos = startingPos;
    }

    public BoundingBox getRegion() {
        return region;
    }

    public Position getStartingPos() {
        return startingPos;
    }

    public boolean hasLootData() {
        return lootData != null;
    }

    public LootData getLootData() {
        return lootData = FtcUtils.makeIfNull(lootData, LootData::new);
    }

    public void setLootData(LootData lootData) {
        this.lootData = lootData;
    }

    public MobData getMobData() {
        return mobData = FtcUtils.makeIfNull(mobData, MobData::new);
    }

    public boolean hasMobData() {
        return mobData != null;
    }

    public void setMobData(MobData mobData) {
        this.mobData = mobData;
    }

    public TriggerData getTriggerData() {
        return triggerData = FtcUtils.makeIfNull(triggerData, TriggerData::new);
    }

    public boolean hasTriggerData() {
        return triggerData != null;
    }

    public void setTriggerData(TriggerData triggerData) {
        this.triggerData = triggerData;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }
}
