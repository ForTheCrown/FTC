package net.forthecrown.utils;


import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.UUID;
import lombok.Data;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.TypeIds;
import net.forthecrown.utils.math.Vectors;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.spongepowered.math.vector.Vector2i;

/**
 * A reference to an entity.
 * <p>
 * Entities tend to dissappear or references to those entities become invalid if the underlying
 * entity is unloaded. This class aims to solve that by not providing a direct reference to an
 * entity, but rather groups 3 values that can be used to locate an entity.
 * <p>
 * If you know where an entity is, then it being unloaded is no problem, simply force load the chunk
 * the entity is inside and then you can access and transform that entity however needed
 */
@Data(staticConstructor = "of")
public class EntityRef {

  public static final char FIELD_SEPARATOR = ' ';

  public static final Codec<EntityRef> CODEC = Codec.STRING.comapFlatMap(s -> {
    try {
      return DataResult.success(EntityRef.parse(s));
    } catch (IllegalArgumentException exc) {
      return DataResult.error(exc::getMessage);
    }
  }, EntityRef::toString);

  private final UUID uniqueId;
  private final String worldName;
  private final Vector2i chunk;

  public World getWorld() {
    return Bukkit.getWorld(getWorldName());
  }

  public Entity get() {
    World w = getWorld();
    if (w == null) {
      return null;
    }

    Chunk c = w.getChunkAt(chunk.x(), chunk.y());
    var result = w.getEntity(getUniqueId());

    if (result != null) {
      return result;
    }

    Entity[] e = c.getEntities();

    for (Entity entity : e) {
      if (entity.getUniqueId().equals(uniqueId)) {
        return entity;
      }
    }

    return null;
  }

  public static EntityRef of(Entity e) {
    Location l = e.getLocation();
    Vector2i chunkPos = Vector2i.from(
        Vectors.toChunk(l.getBlockX()),
        Vectors.toChunk(l.getBlockZ())
    );

    var identifier = new EntityRef(
        e.getUniqueId(),
        l.getWorld().getName(),
        chunkPos
    );

    return identifier;
  }

  public static EntityRef load(BinaryTag t) {
    if (t.getId() == TypeIds.STRING) {
      return parse(t.toString());
    }

    CompoundTag tag = (CompoundTag) t;

    return of(
        tag.getUUID("uuid"),
        tag.getString("world_name"),
        Vectors.fromChunkLong(tag.getLong("chunk"))
    );
  }

  public static EntityRef parse(String input) {
    try {
      StringReader reader = new StringReader(input);
      return parse(reader);
    } catch (CommandSyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static EntityRef parse(StringReader reader) throws CommandSyntaxException {
    String worldName = reader.readString();
    reader.expect(FIELD_SEPARATOR);

    int chunkX = reader.readInt();
    reader.expect(FIELD_SEPARATOR);

    int chunkZ = reader.readInt();
    reader.expect(FIELD_SEPARATOR);

    UUID id = ArgumentTypes.uuid().parse(reader);

    return new EntityRef(id, worldName, new Vector2i(chunkX, chunkZ));
  }

  @Override
  public String toString() {
    return worldName
        + FIELD_SEPARATOR + chunk.x()
        + FIELD_SEPARATOR + chunk.y()
        + FIELD_SEPARATOR + uniqueId;
  }

  public BinaryTag save() {
    return BinaryTags.stringTag(toString());
  }
}