package net.forthecrown.structure;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.serializer.NbtSerializable;
import net.forthecrown.utils.Bukkit2NMS;
import net.forthecrown.utils.FtcBlockData;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.Vector3i;
import net.forthecrown.utils.transformation.FtcBoundingBox;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A structure of blocks that can be placed within a world
 */
public class BlockStructure implements NbtSerializable, Keyed {
    private final Key key;
    private final Map<BlockState, BlockPalette> palettes = new Object2ObjectOpenHashMap<>();

    public BlockStructure(Key key) {
        this.key = FtcUtils.checkNotBukkit(key);
    }

    /**
     * Scans the given area for blocks which will be added into
     * this structure
     *
     * @param world The world to perform the scan in
     * @param size The dimensions of the scan
     * @param start The origin point of the scan
     *
     * @param filter The filter, may be null. This will determine
     *               which blocks are added into the structure
     */
    public void scanFromWorld(World world, Vector3i size, Vector3i start, @Nullable Predicate<Block> filter) {
        clear();
        FtcBoundingBox box = FtcBoundingBox.of(world, start, start.clone().add(size));

        for (Block b: box) {
            if (filter != null && !filter.test(b)) continue;

            Vector3i offset = Vector3i.of(b).subtract(start);
            BlockState state = Bukkit2NMS.getState(b);
            CompoundTag tag = null;

            if(b.getState() instanceof TileState tileState) {
                BlockEntity entity = Bukkit2NMS.getBlockEntity(tileState);
                tag = entity.saveWithoutMetadata();
            }

            add(state, tag, offset);
        }
    }

    /**
     * Adds the given block into the structure
     * @param state The state to add
     * @param tag The NBT data of the block, may be null
     * @param offset The offset of the block from the structure origin
     */
    public void add(BlockState state, @Nullable CompoundTag tag, Vector3i offset) {
        BlockPalette palette = palettes.computeIfAbsent(state, BlockPalette::new);
        palette.add(offset, tag);
    }

    /**
     * Places this structure with the given placement context
     * @param context The context to use when placing
     */
    public void place(StructurePlaceContext context) {
        if(palettes.isEmpty()) return;

        // Create block placement data
        List<BlockPlaceData> placeData = new ObjectArrayList<>();
        for (BlockPalette p: palettes.values()) {
            // If the palette doesn't have any state data
            // skip it... why tf would there be an empty list in here
            if(p.getStateData().isEmpty()) continue;

            // Run through all the state data instances and process them
            for (BlockPalette.StateData d: p.getStateData()) {
                BlockPlaceData data = context.runProccessors(p, d);
                if(data != null) placeData.add(data);
            }
        }

        // Actually place the blocks using the context's BlockPlacer
        for (BlockPlaceData d: placeData) {
            FtcBlockData placeable = d.toPlaceable();
            context.getPlacer().place(d.absolutePos(), placeable);
        }
    }

    public void clear() {
        palettes.clear();
    }

    @Override
    public ListTag save() {
        ListTag list = new ListTag();

        for (BlockPalette p: palettes.values()) {
            list.add(p.save());
        }

        return list;
    }

    public void load(ListTag tag) {
        for (Tag t: tag) {
            CompoundTag data = (CompoundTag) t;

            BlockPalette palette = new BlockPalette(data);
            this.palettes.put(palette.getState(), palette);
        }
    }

    @Override
    public @NotNull Key key() {
        return key;
    }
}
