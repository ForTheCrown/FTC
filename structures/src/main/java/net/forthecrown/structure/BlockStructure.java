package net.forthecrown.structure;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.utils.io.TagUtil;
import org.spongepowered.math.vector.Vector3i;

@RequiredArgsConstructor
public class BlockStructure {
  /* ----------------------------- TAG KEYS ------------------------------- */

  public static final String
      TAG_DATA_VERSION = "data_version",
      TAG_HEADER = "header",
      TAG_PALETTES = "palettes",
      TAG_FUNCTIONS = "functions",

  DEFAULT_PALETTE_NAME = "default";

  /* -------------------------- INSTANCE FIELDS --------------------------- */

  /**
   * A broad header for storing any data
   */
  @Getter
  private final CompoundTag header = BinaryTags.compoundTag();

  /**
   * A map of palette name to palette, a palette is essentially a variation of the structure, most
   * simple structures that have no variants will only have 1 entry in this map, by the name of
   * {@link #DEFAULT_PALETTE_NAME}.
   */
  @Getter
  private final Map<String, BlockPalette> palettes
      = new Object2ObjectOpenHashMap<>();

  final List<FunctionInfo> functions = new ObjectArrayList<>();

  /**
   * The size of the default palette of this structure
   */
  @Getter
  Vector3i defaultSize = Vector3i.ZERO;

  /* ------------------------------ METHODS ------------------------------- */

  /**
   * Clears this structure completely, removing all functions, palettes and emptying the header
   */
  public void clear() {
    palettes.clear();
    functions.clear();
    header.clear();
    defaultSize = Vector3i.ZERO;
  }

  public BlockPalette getPalette(String name) {
    return palettes.get(name == null ? DEFAULT_PALETTE_NAME : name);
  }

  public List<FunctionInfo> getFunctions() {
    return Collections.unmodifiableList(functions);
  }

  /* ----------------------- PLACEMENT AND FILLING ------------------------ */

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
            String.format(
                "Palette size mismatch given: %s, required: %s",
                scanSize, defaultSize
            )
        );
      }
    } else {
      this.defaultSize = config.getArea().size();
    }

    palette.fill(config);
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  public void save(CompoundTag tag) {
    if (!header.isEmpty()) {
      tag.put(TAG_HEADER, header);
    }

    if (!palettes.isEmpty()) {
      CompoundTag paletteTag = BinaryTags.compoundTag();

      for (var e : palettes.entrySet()) {
        CompoundTag pTag = BinaryTags.compoundTag();
        e.getValue().save(pTag);

        paletteTag.put(e.getKey(), pTag);
      }

      tag.put(TAG_PALETTES, paletteTag);
    }

    if (!functions.isEmpty()) {
      tag.put(
          TAG_FUNCTIONS,
          TagUtil.writeList(functions, FunctionInfo::save)
      );
    }
  }

  public void load(CompoundTag tag) {
    clear();

    if (tag.containsKey(TAG_HEADER)) {
      this.header.merge(tag.get(TAG_HEADER).asCompound());
    }

    if (tag.containsKey(TAG_PALETTES)) {
      for (var e : tag.getCompound(TAG_PALETTES).entrySet()) {
        String name = e.getKey();
        CompoundTag pTag = e.getValue().asCompound();

        BlockPalette palette = new BlockPalette(this);
        palette.load(pTag);

        if (name.equals(DEFAULT_PALETTE_NAME)) {
          defaultSize = palette.getSize();
        }

        palettes.put(name, palette);
      }
    }

    if (tag.containsKey(TAG_FUNCTIONS)) {
      functions.addAll(TagUtil.readList(
          tag.get(TAG_FUNCTIONS).asList(),
          FunctionInfo::load
      ));
    }
  }
}