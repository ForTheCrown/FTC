package net.forthecrown.dungeons.level;

import static net.forthecrown.dungeons.level.DungeonPiece.TAG_CHILDREN;
import static net.forthecrown.dungeons.level.DungeonPiece.TAG_TYPE;

import com.google.common.collect.Iterators;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.dungeons.DungeonWorld;
import net.forthecrown.dungeons.level.placement.LevelPlacement;
import net.forthecrown.dungeons.level.room.RoomFlag;
import net.forthecrown.dungeons.level.room.RoomPiece;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.ListTag;
import net.forthecrown.nbt.TagTypes;
import net.forthecrown.utils.ChunkedMap;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.math.Bounds3i;
import org.apache.logging.log4j.Logger;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class DungeonLevel implements Iterable<DungeonPiece> {
  /* ----------------------------- CONSTANTS ------------------------------ */

  private static final Logger LOGGER = Loggers.getLogger();

  public static final String
      TAG_PIECES = "pieces",
      TAG_ROOT = "root",
      TAG_BOSS_ROOM = "bossRoom";

  /* -------------------------- INSTANCE FIELDS --------------------------- */

  /**
   * Piece ID to piece lookup map
   */
  private final Map<UUID, DungeonPiece> pieceLookup
      = new Object2ObjectOpenHashMap<>();

  @Getter
  private final ChunkedMap<DungeonPiece> chunkMap = new ChunkedMap<>();

  @Getter
  private final Set<RoomPiece> activePieces = new ObjectOpenHashSet<>();

  @Getter
  private final Set<RoomPiece> inactivePieces = new ObjectOpenHashSet<>();

  /**
   * The root room from which all other rooms have sprung
   */
  @Getter
  private RoomPiece root;

  @Getter
  @Setter
  private RoomPiece bossRoom;

  private BukkitTask tickTask;

  private final LevelListener listener = new LevelListener(this);

  /* ------------------------------ METHODS ------------------------------- */

  /**
   * Adds this piece and ALL of its children to this level.
   * <p>
   * This method will recurse until all descendant nodes of the given piece have been added to this
   * level.
   * <p>
   * If the current root is null, and the given piece is a root-type piece, the given piece will be
   * set as the new root piece
   *
   * @param piece The piece to add
   */
  public void addPiece(DungeonPiece piece) {
    if (this.root == null
        && piece instanceof RoomPiece room
        && room.getType().hasFlag(RoomFlag.ROOT)
    ) {
      this.root = room;
    }

    pieceLookup.put(piece.getId(), piece);
    piece.level = this;

    if (piece instanceof RoomPiece) {
      chunkMap.add(piece);
    }

    for (var c : piece.getChildren().values()) {
      addPiece(c);
    }
  }

  /**
   * Gets all pieces that intersect with the given piece
   *
   * @param area The area to get the intersecting pieces of
   * @return All pieces that intersect the input
   */
  public Set<DungeonPiece> getIntersecting(Bounds3i area) {
    return chunkMap.getOverlapping(area);
  }

  public void tick() {
    var world = DungeonWorld.get();

    inactivePieces.forEach(piece -> piece.onIdleTick(world, this));
    activePieces.forEach(piece -> piece.onTick(world, this));
  }

  public void startTicking() {
    stopTicking();
    tickTask = Tasks.runTimer(this::tick, 1, 1);
  }

  public void stopTicking() {
    tickTask = Tasks.cancel(tickTask);
    inactivePieces.clear();
    activePieces.clear();
  }

  void onActivate() {
    startTicking();
    listener.register();

    inactivePieces.addAll(
        pieceLookup.values()
            .stream()
            .filter(dungeonPiece -> dungeonPiece instanceof RoomPiece)
            .map(dungeonPiece -> (RoomPiece) dungeonPiece)
            .toList()
    );
  }

  void onDeactivate() {
    stopTicking();
    listener.unregister();
  }

  /* ----------------------------- PLACEMENT ------------------------------ */

  public CompletableFuture<Void> place() {
    return LevelPlacement.create(DungeonWorld.get(), this).run();
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  public void save(CompoundTag tag) {
    ListTag pieces = savePieces();
    tag.put(TAG_PIECES, pieces);
    tag.putUUID(TAG_ROOT, root.getId());

    if (bossRoom != null) {
      tag.putUUID(TAG_BOSS_ROOM, bossRoom.getId());
    }
  }

  public void load(CompoundTag tag) {
    chunkMap.clear();
    pieceLookup.clear();
    root = null;

    loadPieces(
        tag.getList(TAG_PIECES, TagTypes.compoundType()),
        tag.getUUID(TAG_ROOT)
    );

    if (tag.contains(TAG_BOSS_ROOM)) {
      setBossRoom((RoomPiece) pieceLookup.get(tag.getUUID(TAG_BOSS_ROOM)));
    }
  }

  // Note on piece serialization:
  //
  // This overly elaborate-ass piece serialization format is
  // the result of my attempts at ensuring the NBT max tag
  // depth is never reached by serializing all pieces into
  // a flat list and then, during loading, relinking all
  // the pieces using their UUIDs.
  //
  // Which occurs in the loadPieces(Tag, UUID) method,
  // which loads all the pieces from the flat List tag and
  // then uses a temporary Set<Pair> and Map<UUID, Piece>
  // combo to relink them before calling addPiece()
  // and having them added to the level properly.
  //    -- Jules

  private ListTag savePieces() {
    ListTag pieces = BinaryTags.listTag();
    for (var p : this) {
      CompoundTag pTag = BinaryTags.compoundTag();

      p.save(pTag);
      pTag.put(TAG_TYPE, Pieces.save(p.getType()));

      var children = p.getChildren();

      if (!children.isEmpty()) {
        ListTag childTag = BinaryTags.listTag();

        for (var c : children.keySet()) {
          childTag.add(TagUtil.writeUUID(c));
        }

        pTag.put(TAG_CHILDREN, childTag);
      }

      pieces.add(pTag);
    }

    return pieces;
  }

  private void loadPieces(ListTag tag, UUID rootId) {
    Map<UUID, DungeonPiece> linkMap = new Object2ObjectOpenHashMap<>();
    Set<Pair<DungeonPiece, Set<UUID>>>
        piecesAndChildren = new ObjectOpenHashSet<>();

    for (var e : tag) {
      CompoundTag pTag = (CompoundTag) e;

      @SuppressWarnings("rawtypes")
      PieceType type = Pieces.load(pTag.get(TAG_TYPE));

      DungeonPiece piece = type.load(pTag);
      Set<UUID> children = new ObjectOpenHashSet<>();

      if (pTag.contains(TAG_CHILDREN, TagTypes.listType())) {
        children.addAll(
            TagUtil.readList(
                pTag.get(TAG_CHILDREN),
                TagUtil::readUUID
            )
        );
      }

      linkMap.put(piece.getId(), piece);

      if (!children.isEmpty()) {
        piecesAndChildren.add(Pair.of(piece, children));
      }
    }

    for (var pair : piecesAndChildren) {
      var piece = pair.getFirst();

      for (var child : pair.getSecond()) {
        var childPiece = linkMap.get(child);

        if (childPiece == null) {
          LOGGER.warn(
              "Missing child mapping for ID: '{}', parent: '{}'",
              child, piece.getId()
          );

          continue;
        }

        piece.addChild(childPiece);
      }
    }

    RoomPiece rootPiece = (RoomPiece) linkMap.get(rootId);

    if (rootPiece == null) {
      LOGGER.warn(
          "No root piece in NBT! Cannot load piece data, " +
              "no root under ID: {}",
          rootId
      );

      return;
    }

    addPiece(rootPiece);
  }

  @NotNull
  @Override
  public Iterator<DungeonPiece> iterator() {
    return Iterators.unmodifiableIterator(pieceLookup.values().iterator());
  }
}