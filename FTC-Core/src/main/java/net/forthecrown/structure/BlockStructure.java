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
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * A structure of blocks that can be placed within a world
 */
public class BlockStructure implements NbtSerializable, Keyed {
    public static String
            PALETTE_TAG = "palettes",
            ENTITY_TAG = "entities",
            HEADER_TAG = "header";

    private final Key key;
    private final Map<BlockState, BlockPalette> palettes = new Object2ObjectOpenHashMap<>();
    private final List<StructureEntityInfo> entityInfos = new ObjectArrayList<>();
    private final CompoundTag header = new CompoundTag();

    public BlockStructure(Key key) {
        this.key = FtcUtils.ensureBukkit(key);
    }

    /**
     * Scans the given area for blocks which will be added into
     * this structure
     */
    public void scanFromWorld(StructureScanContext context) {
        clear();

        FtcBoundingBox box = FtcBoundingBox.of(context.world(), context.start(), context.start().clone().add(context.size()));
        Vector3i start = context.start();

        for (Block b: box) {
            if (!context.filterBlock(b)) continue;

            Vector3i offset = Vector3i.of(b).subtract(start);
            BlockState state = Bukkit2NMS.getState(b);
            CompoundTag tag = null;

            if(b.getState() instanceof TileState tileState) {
                BlockEntity entity = Bukkit2NMS.getBlockEntity(tileState);
                tag = entity.saveWithoutMetadata();
            }

            add(state, tag, offset);
        }

        if(context.includeEntities()) {
            for (Entity e: box.getEntities()) {
                if(!context.filterEntity(e)) continue;

                Location l = e.getLocation();
                Vec3 offset = new Vec3(
                        l.getX() - start.getX(),
                        l.getY() - start.getY(),
                        l.getZ() - start.getZ()
                );

                net.minecraft.world.entity.Entity entity = Bukkit2NMS.getEntity(e);
                CompoundTag data = new CompoundTag();
                data.putString("id", entity.getMinecraftKeyString());
                data = entity.saveWithoutId(data);

                entityInfos.add(new StructureEntityInfo(offset, data));
            }
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

        if(entityInfos.isEmpty() || !context.placeEntities()) return;
    }

    public void clear() {
        entityInfos.clear();
        palettes.clear();
    }

    public CompoundTag getHeader() {
        return header;
    }

    @Override
    public Tag save() {
        if(entityInfos.isEmpty() && header.isEmpty()) return savePalettes();

        CompoundTag result = new CompoundTag();

        if(!header.isEmpty()) result.put(HEADER_TAG, header);
        if(!entityInfos.isEmpty()) result.put(ENTITY_TAG, saveEntities());

        result.put(PALETTE_TAG, savePalettes());

        return result;
    }

    private ListTag saveEntities() {
        ListTag list = new ListTag();

        for (StructureEntityInfo e: entityInfos) {
            list.add(e.save());
        }

        return list;
    }

    private ListTag savePalettes() {
        ListTag list = new ListTag();

        for (BlockPalette p: palettes.values()) {
            list.add(p.save());
        }

        return list;
    }

    public void load(Tag tag) {
        if (tag.getId() == Tag.TAG_LIST) {
            loadPalettes((ListTag) tag);
            return;
        }

        CompoundTag data = (CompoundTag) tag;

        header.tags.clear();
        header.merge(data.getCompound(HEADER_TAG));

        loadPalettes(data.getList(PALETTE_TAG, Tag.TAG_COMPOUND));

        if(data.contains(ENTITY_TAG)) {
            loadEntities(data.getList(ENTITY_TAG, Tag.TAG_COMPOUND));
        }
    }

    private void loadEntities(ListTag tag) {
        for (Tag t: tag) {
            CompoundTag data = (CompoundTag) t;
            this.entityInfos.add(StructureEntityInfo.of(data));
        }
    }

    private void loadPalettes(ListTag tag) {
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
