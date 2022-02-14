package net.forthecrown.structure;

import net.forthecrown.serializer.NbtSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public record StructureEntityInfo(Vec3 offset, CompoundTag entityData) implements NbtSerializable {
    public static StructureEntityInfo of(CompoundTag data) {
        Vec3 offset = new Vec3(
                data.getDouble("X"),
                data.getDouble("Y"), // YOU'VE WRITTEN THIS 3 MILLION TIMES
                data.getDouble("Z")  // YOU FUCKING DUMBASS, HOW DO YOU STILL MIX UP Y AND Z
        );

        data.remove("Pos");
        data.remove("X");
        data.remove("Y");
        data.remove("Z");

        return new StructureEntityInfo(offset, data);
    }

    @Override
    public CompoundTag save() {
        CompoundTag data = this.entityData.copy();
        data.remove("Pos");

        data.putDouble("X", offset.x);
        data.putDouble("Y", offset.y);
        data.putDouble("Z", offset.z);

        return data;
    }
}
