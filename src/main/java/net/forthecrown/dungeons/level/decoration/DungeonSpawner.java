package net.forthecrown.dungeons.level.decoration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.VanillaAccess;
import net.forthecrown.utils.math.Vectors;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
        CompoundTag tag = new CompoundTag();
        tag.put(TAG_SPAWNER, spawner.save(new CompoundTag()));
        tag.put(TAG_POSITION, Vectors.writeTag(position));
        return tag;
    }

    public static DungeonSpawner load(Tag t) {
        if (!(t instanceof CompoundTag tag)) {
            return null;
        }

        SpawnerImpl spawner = new SpawnerImpl();
        spawner.load(null, null, tag.getCompound(TAG_SPAWNER));

        Vector3i pos = Vectors.read3i(tag.get(TAG_POSITION));

        return new DungeonSpawner(spawner, pos);
    }

    public static class SpawnerImpl extends BaseSpawner {
        @Override
        public void broadcastEvent(Level world, BlockPos pos, int status) {
        }
    }
}