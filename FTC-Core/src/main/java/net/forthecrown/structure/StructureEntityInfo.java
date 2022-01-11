package net.forthecrown.structure;

import net.forthecrown.serializer.NbtSerializable;
import net.minecraft.commands.CommandSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public record StructureEntityInfo(Vec3 offset, CompoundTag data) implements NbtSerializable {
    public static StructureEntityInfo of(CompoundTag data) {
        Vec3 offset = new Vec3(
                data.getDouble("X"),
                data.getDouble("Z"),
                data.getDouble("Y")
        );

        data.remove("X");
        data.remove("Y");
        data.remove("Z");

        return new StructureEntityInfo(offset, data);
    }

    @Override
    public CompoundTag save() {
        CompoundTag data = this.data.copy();
        data.putDouble("X", offset.x);
        data.putDouble("Y", offset.y);
        data.putDouble("Z", offset.z);

        return data;
    }
}
