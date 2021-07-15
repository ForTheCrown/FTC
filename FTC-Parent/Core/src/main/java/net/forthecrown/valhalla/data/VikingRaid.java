package net.forthecrown.valhalla.data;

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
        return lootData == null ? lootData = new LootData() : lootData;
    }

    public void setLootData(LootData lootData) {
        this.lootData = lootData;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }
}
