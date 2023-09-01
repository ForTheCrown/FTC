package net.forthecrown.structure;

import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.ListTag;
import net.forthecrown.nbt.TagTypes;
import net.forthecrown.nbt.paper.PaperNbt;
import net.forthecrown.utils.math.Rotation;
import net.forthecrown.utils.math.Vectors;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import org.spongepowered.math.vector.Vector3d;

public interface StructureEntitySpawner {
  void addEntity(Vector3d absPos, Rotation rotation, EntityType type, CompoundTag tag);

  static StructureEntitySpawner world(World world) {
    return (absPos, rotation, type, tag) -> {
      placeEntity(world, absPos, rotation, type, tag);
    };
  }

  static void placeEntity(
      World world,
      Vector3d pos,
      Rotation rotation,
      EntityType type,
      CompoundTag tag
  ) {
    tag = tag.copy();

    ListTag posList = tag.getList("Pos", TagTypes.doubleType());
    ListTag rotList = tag.getList("Rotation", TagTypes.floatType());

    double x = posList.getDouble(0);
    double y = posList.getDouble(1);
    double z = posList.getDouble(2);

    float yaw = rotList.getFloat(0);
    float pitch = rotList.getFloat(1);

    Location location = new Location(world, x, y, z, yaw, pitch);
    Vector dir = location.getDirection();

    Vector3d mathDir = Vectors.doubleFrom(dir);
    mathDir = rotation.rotate(mathDir);

    dir = Vectors.toVec(mathDir);
    location.setDirection(dir);

    posList = BinaryTags.doubleList(pos.x(), pos.y(), pos.z());
    rotList = BinaryTags.floatList(location.getYaw(), location.getPitch());

    tag.put("Pos", posList);
    tag.put("Rotation", rotList);

    Entity entity = world.spawnEntity(location, type);
    PaperNbt.loadEntity(entity, tag);
  }
}