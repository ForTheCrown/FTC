package net.forthecrown.valhalla.data;

import net.forthecrown.valhalla.RaidGenerationContext;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.apache.commons.lang.Validate;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;

import javax.annotation.Nullable;

public class EntitySpawnData {
    private final NamespacedKey entityKey;
    private final CompoundTag tag;
    private final Position position;

    public EntitySpawnData(NamespacedKey entityKey, CompoundTag tag, Position position) {
        this.entityKey = entityKey;
        this.tag = tag;
        this.position = position;
    }

    public void spawn(RaidGenerationContext context) {
        CompoundTag spawnData = tag == null ? new CompoundTag() : tag.copy();
        spawnData.putString("id", entityKey.asString());

        ServerLevel level = ((CraftWorld) context.getWorld()).getHandle();

        Entity entity = EntityType.loadEntityRecursive(spawnData, level, ent -> ent);
        Validate.notNull(entity, "Entity is null, invalid NBT?");

        level.addAllEntitiesSafely(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    public @Nullable CompoundTag getTag() {
        return tag;
    }

    public boolean hasTag() {
        return tag != null;
    }

    public NamespacedKey getEntityKey() {
        return entityKey;
    }

    public Position getPosition() {
        return position;
    }
}
