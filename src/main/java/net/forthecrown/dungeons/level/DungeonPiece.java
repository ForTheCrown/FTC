package net.forthecrown.dungeons.level;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import net.forthecrown.dungeons.level.gate.DungeonGate;
import net.forthecrown.dungeons.level.generator.NodeAlign;
import net.forthecrown.structure.BlockProcessors;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.Rotation;
import net.forthecrown.structure.StructurePlaceConfig;
import net.forthecrown.user.User;
import net.forthecrown.utils.BoundsHolder;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Transform;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.World;
import org.spongepowered.math.vector.Vector3i;

public abstract class DungeonPiece implements BoundsHolder {
  /* ----------------------------- CONSTANTS ------------------------------ */

  public static final String
      TAG_ID = "id",
      TAG_TYPE = "type",
      TAG_ROTATION = "rotation",
      TAG_BOUNDS = "bounds",
      TAG_CHILDREN = "children",
      TAG_DEPTH = "depth",
      TAG_PALETTE = "palette";

  public static final int STARTING_DEPTH = 1;

  /* ----------------------------- INSTANCE FIELDS ------------------------------ */

  /**
   * Randomly generated ID of the piece
   */
  @Getter
  private final UUID id;

  /**
   * Piece's type
   */
  @Getter
  private final PieceType<? extends DungeonPiece> type;

  private Bounds3i bounds;

  @Getter
  private int depth = STARTING_DEPTH;

  @Getter
  private DungeonPiece parent;

  protected final Object2ObjectMap<UUID, DungeonPiece>
      children = new Object2ObjectOpenHashMap<>();

  @Getter
  private Rotation rotation = Rotation.NONE;

  /**
   * List of users inside this room
   */
  @Getter
  private final List<User> users = new ObjectArrayList<>();

  /**
   * The level this piece is apart of
   */
  @Getter
  DungeonLevel level;

  /* ----------------------------- CONSTRUCTORS ------------------------------ */

  public DungeonPiece(PieceType type) {
    this.type = type;
    this.id = UUID.randomUUID();
  }

  public DungeonPiece(PieceType type, CompoundTag tag) {
    this.type = type;
    this.id = tag.getUUID(TAG_ID);

    if (tag.contains(TAG_ROTATION)) {
      this.rotation = TagUtil.readEnum(Rotation.class, tag.get(TAG_ROTATION));
    }

    if (tag.contains(TAG_BOUNDS)) {
      this.bounds = Bounds3i.of(tag.get(TAG_BOUNDS));
    }

    if (tag.contains(TAG_DEPTH)) {
      setDepth(tag.getInt(TAG_DEPTH));
    }
  }

  /* ----------------------------- METHODS ------------------------------ */

  public void place(World world) {
    var struct = getStructure();

    if (struct == null) {
      return;
    }

    StructurePlaceConfig config = createPlaceConfig(world).build();
    struct.place(config);
  }

  public BlockStructure getStructure() {
    return getType().getStructure().orElse(null);
  }

  public Vector3i getStructureSize() {
    return getStructure().getDefaultSize();
  }

  protected StructurePlaceConfig.Builder createPlaceConfig(World world) {
    return StructurePlaceConfig.builder()
        .addRotationProcessor()
        .addNonNullProcessor()
        .addProcessor(BlockProcessors.IGNORE_AIR)
        .pos(getPivotPosition())
        .paletteName(getPaletteName())
        .transform(Transform.rotation(getRotation()))
        .world(world)
        .placeEntities(true);
  }

  public abstract String getPaletteName();

  public Bounds3i getBounds() {
    return this.bounds == null ? bounds = getLocalizedBounds() : bounds;
  }

  public Bounds3i getLocalizedBounds() {
    return Bounds3i.of(Vector3i.ZERO, getStructureSize().sub(Vector3i.ONE));
  }

  public void apply(Transform transform) {
    if (level != null) {
      level.getChunkMap().remove(this);
    }

    if (transform.getRotation() != Rotation.NONE) {
      this.rotation = this.rotation.add(transform.getRotation());
    }

    Bounds3i b = getBounds();
    this.bounds = Bounds3i.of(
        transform.apply(b.min()),
        transform.apply(b.max())
    );

    if (level != null) {
      level.getChunkMap().add(this);
    }
  }

  public Vector3i getPivotPosition() {
    return NodeAlign.pivotPoint(getBounds(), rotation);
  }

  public String debugInfo() {
    StringBuffer buffer = new StringBuffer();
    DebugVisitor walker = new DebugVisitor(buffer);

    visit(walker);
    return buffer.toString();
  }

  /* ----------------------------- CHILD MANAGEMENT ------------------------------ */

  public boolean addChild(DungeonPiece piece) {
    if (!canBeChild(piece)
        || children.containsKey(piece.getId())
        || piece.getParent() != null
    ) {
      return false;
    }

    children.put(piece.getId(), piece);
    piece.parent = this;

    // Exits keep the depth of their parent
    if (piece instanceof DungeonGate) {
      piece.setDepth(getDepth());
    } else {
      piece.setDepth(getDepth() + 1);
    }

    return true;
  }

  public boolean removeChild(DungeonPiece piece) {
    return removeChild(piece.getId());
  }

  public boolean removeChild(UUID uuid) {
    var removed = children.remove(uuid);

    if (removed == null) {
      return false;
    }

    removed.setDepth(0);
    removed.parent = null;

    return true;
  }

  public void clearChildren() {
    children.values()
        .stream()
        .forEach(this::removeChild);
  }

  public boolean hasChildren() {
    return !children.isEmpty();
  }

  public Map<UUID, DungeonPiece> getChildren() {
    return children.isEmpty()
        ? Object2ObjectMaps.emptyMap()
        : Object2ObjectMaps.unmodifiable(children);
  }

  public void setDepth(int depth) {
    this.depth = depth;

    // Propagate depth change to children
    if (!this.children.isEmpty()) {
      for (var c : children.values()) {

        // Gates keep the depth of their parents
        if (c instanceof DungeonGate) {
          c.setDepth(depth);
        } else {
          c.setDepth(depth + 1);
        }
      }
    }
  }

  protected boolean canBeChild(DungeonPiece o) {
    return o instanceof DungeonGate;
  }

  /* ----------------------------- CALLBACKS ------------------------------ */

  /**
   * Ticked whenever 1 or more players are inside the room
   */
  public void onTick(World world, DungeonLevel level) {

  }

  /**
   * Ticked whenever the room is empty
   */
  public void onIdleTick(World world, DungeonLevel level) {

  }

  public boolean isTicked() {
    return false;
  }

  public void onEnter(User user, DungeonLevel level) {

  }

  public void onExit(User user, DungeonLevel level) {

  }

  /* ----------------------------- ITERATION ------------------------------ */

  public PieceVisitor.Result visit(PieceVisitor walker) {
    walker.onPieceStart(this);
    var initial = onVisit(walker);

    if (initial == PieceVisitor.Result.STOP) {
      walker.onPieceEnd(this);
      return PieceVisitor.Result.STOP;
    }

    if (initial == PieceVisitor.Result.SKIP_CHILDREN) {
      walker.onPieceEnd(this);
      return PieceVisitor.Result.CONTINUE;
    }

    var result = visitChildren(walker);
    walker.onPieceEnd(this);

    return result;
  }

  protected abstract PieceVisitor.Result onVisit(PieceVisitor walker);

  protected PieceVisitor.Result visitChildren(PieceVisitor walker) {
    if (!hasChildren()) {
      return PieceVisitor.Result.CONTINUE;
    }

    walker.onChildrenStart(this);

    for (var c : children.values()) {
      if (c.visit(walker) == PieceVisitor.Result.STOP) {
        return PieceVisitor.Result.STOP;
      }
    }

    walker.onChildrenEnd(this);
    return PieceVisitor.Result.CONTINUE;
  }

  /* ----------------------------- SERIALIZATION ------------------------------ */

  public void save(CompoundTag tag) {
    tag.putUUID(TAG_ID, getId());
    tag.put(TAG_ROTATION, TagUtil.writeEnum(getRotation()));
    tag.put(TAG_BOUNDS, getBounds().save());
    tag.putInt(TAG_DEPTH, getDepth());
    tag.putString(TAG_PALETTE, getPaletteName());

    saveAdditional(tag);
  }

  protected abstract void saveAdditional(CompoundTag tag);

  /* ----------------------------- OBJECT OVERRIDES ------------------------------ */

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!getClass().isInstance(o)) {
      return false;
    }

    DungeonPiece piece = (DungeonPiece) o;

    return getId().equals(piece.getId());
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(getId()).toHashCode();
  }
}