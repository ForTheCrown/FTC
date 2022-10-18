package net.forthecrown.dungeons.level;

import lombok.Getter;
import net.minecraft.nbt.CompoundTag;

@Getter
public class RoomType extends PieceType<DungeonRoom> {
    private final int flags;

    public RoomType(String structureName, int flags) {
        super(structureName);
        this.flags = flags;
    }

    public boolean hasFlags(int flags) {
        return (this.flags & flags) == flags;
    }

    @Override
    public DungeonRoom create() {
        return new DungeonRoom(this);
    }

    @Override
    public DungeonRoom load(CompoundTag tag) {
        return new DungeonRoom(this, tag);
    }
}