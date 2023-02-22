package net.forthecrown.dungeons.level.room;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import net.forthecrown.dungeons.level.DungeonLevel;
import net.forthecrown.dungeons.level.DungeonPiece;
import net.forthecrown.dungeons.level.LevelBiome;
import net.forthecrown.dungeons.level.PieceVisitor;
import net.forthecrown.dungeons.level.PieceVisitor.Result;
import net.forthecrown.utils.io.TagUtil;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class RoomPiece extends DungeonPiece {
  public static final String
      TAG_SPAWNERS = "spawners";

  @Getter
  private final List<DungeonSpawner> spawners = new ObjectArrayList<>();

  @Getter
  private final List<Player> players = new LinkedList<>();

  /* ---------------------------- CONSTRUCTORS ---------------------------- */

  public RoomPiece(RoomType type) {
    super(type);
  }

  public RoomPiece(RoomType piece, CompoundTag tag) {
    super(piece, tag);

    if (tag.contains(TAG_SPAWNERS)) {
      spawners.addAll(
          TagUtil.readCollection(tag.get(TAG_SPAWNERS), DungeonSpawner::load)
      );
    }
  }

  /* ------------------------------ METHODS ------------------------------- */

  @Override
  public String getPaletteName(LevelBiome biome) {
    return getType().getPalette(biome);
  }

  @Override
  public RoomType getType() {
    return (RoomType) super.getType();
  }

  @Override
  protected Result onVisit(PieceVisitor walker) {
    return walker.onRoom(this);
  }

  public void onTick(World world, DungeonLevel level) {
    spawners.forEach(spawner -> spawner.onTick(world));
  }

  public void onIdleTick(World world, DungeonLevel level) {
  }

  public void onEnter(Player user, DungeonLevel level) {
  }

  public void onExit(Player user, DungeonLevel level) {
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  @Override
  protected void saveAdditional(CompoundTag tag) {
    if (!spawners.isEmpty()) {
      tag.put(TAG_SPAWNERS,
          TagUtil.writeCollection(spawners, DungeonSpawner::save)
      );
    }
  }
}