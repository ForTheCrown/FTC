package net.forthecrown.dungeons.level.room;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.paper.TagTranslators;
import net.forthecrown.utils.VanillaAccess;
import net.forthecrown.utils.math.Vectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import org.bukkit.World;
import org.spongepowered.math.vector.Vector3i;

@Getter
@RequiredArgsConstructor
public class DungeonSpawner {

  public static final String
      TAG_SPAWNER = "spawner",
      TAG_POSITION = "position";

  private final SpawnerImpl spawner;
  private final Vector3i position;

  public void onTick(World world) {
    spawner.serverTick(
        VanillaAccess.getLevel(world),
        Vectors.toMinecraft(position)
    );
  }

  public CompoundTag save() {
    CompoundTag tag = BinaryTags.compoundTag();

    var spawnerData = TagTranslators.toApi(
        spawner.save(new net.minecraft.nbt.CompoundTag())
    );

    tag.put(TAG_SPAWNER, spawnerData);
    tag.put(TAG_POSITION, Vectors.writeTag(position));
    return tag;
  }

  public static DungeonSpawner load(BinaryTag t) {
    if (!(t instanceof CompoundTag tag)) {
      return null;
    }

    SpawnerImpl spawner = new SpawnerImpl();
    spawner.load(null, null, TagTranslators.COMPOUND.toMinecraft(tag.getCompound(TAG_SPAWNER)));

    Vector3i pos = Vectors.read3i(tag.get(TAG_POSITION));

    return new DungeonSpawner(spawner, pos);
  }

  public static class SpawnerImpl extends BaseSpawner {

    @Override
    public void broadcastEvent(Level world, BlockPos pos, int status) {
    }
  }
}