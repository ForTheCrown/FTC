package net.forthecrown.dungeons.level;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import lombok.Getter;
import net.forthecrown.dungeons.level.decoration.DungeonSpawner;
import net.forthecrown.utils.io.TagUtil;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.World;

@Getter
public class DungeonRoom extends DungeonPiece {

  public static final String
      TAG_SPAWNERS = "spawners";

  private final List<DungeonSpawner> spawners = new ObjectArrayList<>();

  public DungeonRoom(RoomType type) {
    super(type);
  }

  public DungeonRoom(RoomType piece, CompoundTag tag) {
    super(piece, tag);

    if (tag.contains(TAG_SPAWNERS)) {
      spawners.addAll(
          TagUtil.readCollection(tag.get(TAG_SPAWNERS), DungeonSpawner::load)
      );
    }
  }

  @Override
  public String getPaletteName() {
    return getType()
        .getVariant(PieceStyle.DEFAULT);
  }

  @Override
  public RoomType getType() {
    return (RoomType) super.getType();
  }

  @Override
  protected PieceVisitor.Result onVisit(PieceVisitor walker) {
    return walker.onRoom(this);
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    if (!spawners.isEmpty()) {
      tag.put(TAG_SPAWNERS,
          TagUtil.writeCollection(spawners, DungeonSpawner::save)
      );
    }
  }

  @Override
  public void onTick(World world, DungeonLevel level) {
    spawners.forEach(spawner -> spawner.onTick(world));
  }

  @Override
  public boolean isTicked() {
    return true;
  }
}