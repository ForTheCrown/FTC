package net.forthecrown.structure;

import static net.forthecrown.structure.BlockInfo.copyTag;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.paper.PaperNbt;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.math.Transform;
import net.forthecrown.utils.math.Vectors;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.spongepowered.math.vector.Vector3d;

/**
 * Info representing a single scanned entity within a structure
 */
@Getter
@RequiredArgsConstructor
public class EntityInfo {
  /* ----------------------------- CONSTANTS ------------------------------ */

  private static final String
      TAG_POSITION = "position",
      TAG_TYPE = "type",
      TAG_DATA = "data";

  /* ----------------------------- INSTANCE FIELDS ------------------------------ */

  /**
   * The entity's position, relative to the structure
   */
  private final Vector3d position;

  /**
   * The entity's type key
   */
  private final NamespacedKey type;

  /**
   * The entity data
   */
  private final CompoundTag tag;

  /* ----------------------------- METHODS ------------------------------ */

  void place(StructurePlaceConfig config) {
    Transform transform = config.getTransform();
    Vector3d dest = transform.apply(position);

    config.getEntitySpawner().addEntity(
        dest,
        transform.getRotation(),
        Registry.ENTITY_TYPE.get(type),
        copyTag(tag)
    );
  }

  /* ----------------------------- CLONE BUILDERS ------------------------------ */

  public EntityInfo copy() {
    return new EntityInfo(position, type, copyTag(tag));
  }

  public EntityInfo withTag(CompoundTag tag) {
    return new EntityInfo(position, type, copyTag(tag));
  }

  public EntityInfo withPosition(Vector3d position) {
    return new EntityInfo(position, type, copyTag(tag));
  }

  public EntityInfo withType(EntityType type) {
    return new EntityInfo(position, type.getKey(), copyTag(tag));
  }

  public EntityInfo withType(NamespacedKey type) {
    return new EntityInfo(position, type, copyTag(tag));
  }

  /* ----------------------------- SERIALIZATION ------------------------------ */

  public BinaryTag save() {
    CompoundTag tag = BinaryTags.compoundTag();
    tag.put(TAG_POSITION, Vectors.writeTag(position));
    tag.put(TAG_TYPE, TagUtil.writeKey(type));
    tag.put(TAG_DATA, this.tag);

    return tag;
  }

  public static EntityInfo load(BinaryTag t) {
    CompoundTag tag = (CompoundTag) t;

    return new EntityInfo(
        Vectors.read3d(tag.get(TAG_POSITION)),
        TagUtil.readKey(tag.get(TAG_TYPE)),
        tag.getCompound(TAG_DATA)
    );
  }

  /* ----------------------------------------------------------- */

  public static EntityInfo of(Vector3d origin, Entity entity) {
    var entLoc = entity.getLocation();
    Vector3d entityPos = Vector3d.from(entLoc.getX(), entLoc.getY(), entLoc.getZ());
    Vector3d offset = entityPos.sub(origin);

    NamespacedKey typeKey = entity.getType().getKey();
    CompoundTag tag = PaperNbt.saveEntity(entity);

    return new EntityInfo(offset, typeKey, tag);
  }
}