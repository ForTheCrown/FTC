package net.forthecrown.structure;

import net.forthecrown.utils.Bukkit2NMS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.World;
import org.bukkit.event.entity.CreatureSpawnEvent;

public interface EntityPlacer {
    void place(CompoundTag tag, double x, double y, double z);

    default void place(CompoundTag tag, Vec3 vec3) {
        place(tag, vec3.x, vec3.y, vec3.z);
    }

    static EntityPlacer world(World world) {
        return new EntityPlacer() {
            private final ServerLevel level = Bukkit2NMS.getLevel(world);

            @Override
            public void place(CompoundTag tag, double x, double y, double z) {
                Entity entity = EntityType.create(tag, level).orElseThrow(() -> new IllegalArgumentException("Attempted to place unknown entity type in NBT"));
                level.addFreshEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);

                entity.moveTo(x, y, z);
            }
        };
    }
}
