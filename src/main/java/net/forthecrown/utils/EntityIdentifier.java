package net.forthecrown.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Data;
import net.forthecrown.grenadier.types.UUIDArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.UUID;

/**
 * The trouble with entities is finding them, I, Julie,
 * believed there might be an issue with locating entities
 * if they're in a non-loaded chunk, so I created this to
 * provide a way to mark where an entity last was and
 * store enough info about it to find it later.
 */
@Data(staticConstructor = "of")
public class EntityIdentifier {
    public static final char FIELD_SEPARATOR = ' ';

    private final UUID uniqueId;
    private final String worldName;
    private final ChunkPos chunk;

    public World getWorld() {
        return Bukkit.getWorld(getWorldName());
    }

    public Entity get() {
        World w = getWorld();
        if (w == null) return null;

        Chunk c = w.getChunkAt(chunk.x, chunk.z);
        return w.getEntity(getUniqueId());
    }

    public static EntityIdentifier of(Entity e) {
        Location l = e.getLocation();
        ChunkPos chunkPos = new ChunkPos(l.getBlockX() >> 4, l.getBlockZ() >> 4);

        return new EntityIdentifier(e.getUniqueId(), l.getWorld().getName(), chunkPos);
    }

    public static EntityIdentifier load(Tag t) {
        CompoundTag tag = (CompoundTag) t;

        return of(
                tag.getUUID("uuid"),
                tag.getString("world_name"),
                new ChunkPos(tag.getLong("chunk"))
        );
    }

    public static EntityIdentifier parse(String input) {
        try {
            StringReader reader = new StringReader(input);
            return parse(reader);
        } catch (CommandSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static EntityIdentifier parse(StringReader reader) throws CommandSyntaxException {
        String worldName = reader.readString();
        reader.expect(FIELD_SEPARATOR);

        int chunkX = reader.readInt();
        reader.expect(FIELD_SEPARATOR);

        int chunkZ = reader.readInt();

        UUID id = UUIDArgument.uuid().parse(reader);

        return new EntityIdentifier(id, worldName, new ChunkPos(chunkX, chunkZ));
    }

    @Override
    public String toString() {
        return worldName + FIELD_SEPARATOR + chunk.x + FIELD_SEPARATOR + chunk.z + FIELD_SEPARATOR + uniqueId;
    }

    public Tag save() {
        CompoundTag tag = new CompoundTag();

        tag.putUUID("uuid", uniqueId);
        tag.putString("world_name", worldName);
        tag.putLong("chunk", chunk.longKey);

        return tag;
    }
}