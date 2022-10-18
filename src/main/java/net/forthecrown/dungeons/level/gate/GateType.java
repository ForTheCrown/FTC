package net.forthecrown.dungeons.level.gate;

import lombok.Getter;
import net.forthecrown.dungeons.level.PieceType;
import net.minecraft.nbt.CompoundTag;

@Getter
public class GateType extends PieceType<DungeonGate> {
    private final boolean openable;

    public GateType(String structureName, boolean openable) {
        super(structureName);
        this.openable = openable;
    }

    @Override
    public DungeonGate create() {
        return new DungeonGate(this);
    }

    @Override
    public DungeonGate load(CompoundTag tag) {
        return new DungeonGate(this, tag);
    }
}