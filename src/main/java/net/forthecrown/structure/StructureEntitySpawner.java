package net.forthecrown.structure;

import static net.minecraft.world.entity.Entity.ID_TAG;

import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.paper.TagTranslators;
import net.forthecrown.utils.VanillaAccess;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.spongepowered.math.vector.Vector3d;

public interface StructureEntitySpawner {
  void addEntity(Vector3d absPos, Rotation rotation, EntityType type, CompoundTag tag);

  static StructureEntitySpawner world(World world) {
    return (absPos, rotation, type, tag) -> {
      placeEntity(world, absPos, rotation, type, tag);
    };
  }

  static void placeEntity(World world,
                          Vector3d pos,
                          Rotation rotation,
                          EntityType type,
                          CompoundTag tag
  ) {
    var level = VanillaAccess.getLevel(world);

    tag.putString(ID_TAG, type.getKey().asString());

    var opt = net.minecraft.world.entity.EntityType.create(
        TagTranslators.COMPOUND.toMinecraft(tag),
        level
    );

    opt.ifPresent(entity -> {
      entity.moveTo(pos.x(), pos.y(), pos.z());

      var rot = entity.rotate(VanillaAccess.toVanilla(rotation));
      entity.setYRot(rot);

      level.addFreshEntity(entity);
    });
  }
}