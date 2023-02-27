package net.forthecrown.dungeons.level.placement;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.structure.Rotation;
import net.forthecrown.structure.StructureEntitySpawner;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.spongepowered.math.vector.Vector3d;

class DungeonEntityPlacement implements StructureEntitySpawner {
  private final List<EntityInfo> entityInfos = new ObjectArrayList<>();

  void place(World world) {
    entityInfos.forEach(entityInfo -> {
      StructureEntitySpawner.placeEntity(
          world,
          entityInfo.pos,
          entityInfo.rotation,
          entityInfo.type,
          entityInfo.tag
      );
    });

    entityInfos.clear();
  }

  @Override
  public void addEntity(Vector3d absPos,
                        Rotation rotation,
                        EntityType type,
                        CompoundTag tag
  ) {
    entityInfos.add(new EntityInfo(absPos, rotation, type, tag));
  }

  private record EntityInfo(Vector3d pos,
                            Rotation rotation,
                            EntityType type,
                            CompoundTag tag
  ) {

  }
}