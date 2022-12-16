package net.forthecrown.structure;

import com.mojang.serialization.Dynamic;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.VanillaAccess;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.math.Vectors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.spongepowered.math.vector.Vector3d;

/**
 * Info representing a single scanned entity
 * within a structure
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

    /** The entity's position, relative to the structure */
    private final Vector3d position;

    /** The entity's type key */
    private final NamespacedKey type;

    /** The entity data */
    private final CompoundTag tag;

    /* ----------------------------- METHODS ------------------------------ */

    EntityInfo fixData(int oldVersion, int newVersion) {
        return withTag(
                (CompoundTag) DataFixers.getDataFixer()
                        .update(
                                References.ENTITY_TREE,
                                new Dynamic<>(NbtOps.INSTANCE, tag),
                                oldVersion, newVersion
                        )
                        .getValue()
        );
    }

    void place(StructurePlaceConfig config) {
        World world = config.getWorld();
        Vector3d dest = config.getTransform().apply(position);

        CompoundTag tag = this.tag.copy();
        tag.putString(net.minecraft.world.entity.Entity.ID_TAG, type.asString());

        var level = VanillaAccess.getLevel(world);

        net.minecraft.world.entity.EntityType.create(tag, level)
                .ifPresent(entity -> {
                    entity.moveTo(dest.x(), dest.y(), dest.z());

                    Rotation rotation = config.getTransform().getRotation();

                    if (rotation != Rotation.NONE) {
                        float yRot = entity.rotate(VanillaAccess.toVanilla(rotation));
                        entity.setYRot(yRot);
                    }

                    level.addFreshEntity(entity);
                });
    }

    /* ----------------------------- CLONE BUILDERS ------------------------------ */

    public EntityInfo copy() {
        return new EntityInfo(position, type, tag.copy());
    }

    public EntityInfo withTag(CompoundTag tag) {
        return new EntityInfo(position, type, tag.copy());
    }

    public EntityInfo withPosition(Vector3d position) {
        return new EntityInfo(position, type, tag.copy());
    }

    public EntityInfo withType(EntityType type) {
        return new EntityInfo(position, type.getKey(), tag.copy());
    }

    public EntityInfo withType(NamespacedKey type) {
        return new EntityInfo(position, type, tag.copy());
    }

    /* ----------------------------- SERIALIZATION ------------------------------ */

    public Tag save() {
        CompoundTag tag = new CompoundTag();
        tag.put(TAG_POSITION, Vectors.writeTag(position));
        tag.put(TAG_TYPE, TagUtil.writeKey(type));
        tag.put(TAG_DATA, this.tag);

        return tag;
    }

    public static EntityInfo load(Tag t) {
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

        CompoundTag tag = new CompoundTag();
        VanillaAccess.getEntity(entity).saveWithoutId(tag);

        tag.remove(net.minecraft.world.entity.Entity.UUID_TAG);
        tag.remove(net.minecraft.world.entity.Entity.ID_TAG);
        tag.remove("WorldUUIDMost");
        tag.remove("WorldUUIDLeast");
        tag.remove("Pos");

        return new EntityInfo(offset, typeKey, tag);
    }
}