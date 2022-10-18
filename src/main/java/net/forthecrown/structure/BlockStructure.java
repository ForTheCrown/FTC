package net.forthecrown.structure;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.io.TagUtil;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class BlockStructure {
    /* ----------------------------- TAG KEYS ------------------------------ */

    public static final String
            TAG_DATA_VERSION = "data_version",
            TAG_HEADER = "header",
            TAG_PALETTES = "palettes",
            TAG_FUNCTIONS = "functions",

            DEFAULT_PALETTE_NAME = "default";

    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    /** A broad header for storing any data */
    @Getter
    private final CompoundTag header = new CompoundTag();

    /**
     * A map of palette name to palette, a palette is essentially a
     * variation of the structure, most simple structures that have
     * no variants will only have 1 entry in this map, by the name
     * of {@link #DEFAULT_PALETTE_NAME}.
     */
    @Getter
    private final Map<String, BlockPalette> palettes = new Object2ObjectOpenHashMap<>();

    final List<FunctionInfo> functions = new ObjectArrayList<>();

    /** The size of the default palette of this structure */
    @Getter
    Vector3i defaultSize = Vector3i.ZERO;

    /* ----------------------------- METHODS ------------------------------ */

    /**
     * Clears this structure completely, removing all functions,
     * palettes and emptying the header
     */
    public void clear() {
        palettes.clear();
        functions.clear();
        header.tags.clear();
        defaultSize = Vector3i.ZERO;
    }

    public BlockPalette getPalette(String name) {
        return palettes.get(name == null ? DEFAULT_PALETTE_NAME : name);
    }

    public List<FunctionInfo> getFunctions() {
        return Collections.unmodifiableList(functions);
    }

    /* ----------------------------- PLACEMENT AND FILLING ------------------------------ */

    public void place(StructurePlaceConfig config) {
        var palette = getPalette(config.getPaletteName());

        if (palette == null) {
            return;
        }

        palette.place(config);
    }

    public void fill(StructureFillConfig config) {
        var palette = getPalette(config.getPaletteName());

        if (palette == null) {
            palette = new BlockPalette(this);
            palettes.put(config.getPaletteName(), palette);
        }

        if (!DEFAULT_PALETTE_NAME.equals(config.getPaletteName())) {
            var scanSize = config.getArea().size();

            if (!palettes.containsKey(config.getPaletteName())
                    && !defaultSize.equals(Vector3i.ZERO)
                    && !scanSize.equals(getDefaultSize())
            ) {
                throw new IllegalArgumentException(
                        String.format("Palette size mismatch given: %s, required: %s", scanSize, defaultSize)
                );
            }
        } else {
            this.defaultSize = config.getArea().size();
        }

        palette.fill(config);
    }

    /* ----------------------------- SERIALIZATION ------------------------------ */

    public void save(CompoundTag tag) {
        tag.putInt(TAG_DATA_VERSION, getDataVersion());

        if (!header.isEmpty()) {
            tag.put(TAG_HEADER, header);
        }

        if (!palettes.isEmpty()) {
            CompoundTag paletteTag = new CompoundTag();

            for (var e: palettes.entrySet()) {
                CompoundTag pTag = new CompoundTag();
                e.getValue().save(pTag);

                paletteTag.put(e.getKey(), pTag);
            }

            tag.put(TAG_PALETTES, paletteTag);
        }

        if (!functions.isEmpty()) {
            tag.put(TAG_FUNCTIONS, TagUtil.writeCollection(functions, FunctionInfo::save));
        }
    }

    public void load(CompoundTag tag) {
        clear();

        int loadedVersion = tag.getInt(TAG_DATA_VERSION);
        int currentVersion = getDataVersion();

        if (tag.contains(TAG_HEADER)) {
            this.header.merge(tag.getCompound(TAG_HEADER));
        }

        if (tag.contains(TAG_PALETTES)) {
            for (var e: tag.getCompound(TAG_PALETTES).tags.entrySet()) {
                String name = e.getKey();
                CompoundTag pTag = (CompoundTag) e.getValue();

                BlockPalette palette = new BlockPalette(this);
                palette.load(pTag, currentVersion, loadedVersion);

                if (name.equals(DEFAULT_PALETTE_NAME)) {
                    defaultSize = palette.getSize();
                }

                palettes.put(name, palette);
            }
        }

        if (tag.contains(TAG_FUNCTIONS)) {
            functions.addAll(TagUtil.readCollection(
                    tag.get(TAG_FUNCTIONS),
                    FunctionInfo::load
            ));
        }
    }

    private static int getDataVersion() {
        return SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    }
}