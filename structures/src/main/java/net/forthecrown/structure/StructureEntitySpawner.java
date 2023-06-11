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

  static void placeEntity(World world,
                          Vector3d pos,
                          Rotation rotation,
                          EntityType type,
                          CompoundTag tag
  ) {
    tag = tag.copy();

    ListTag posList = tag.getList("Pos", TagTypes.doubleType());
    ListTag rotList = tag.getList("Rotation", TagTypes.floatType());

    double x = posList.get(0).asNumber().doubleValue();
    double y = posList.get(1).asNumber().doubleValue();
    double z = posList.get(2).asNumber().doubleValue();

    float yaw = rotList.get(0).asNumber().floatValue();
    float pitch = rotList.get(1).asNumber().floatValue();

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