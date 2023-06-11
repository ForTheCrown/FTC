package net.forthecrown.dungeons.level.room;

import com.mojang.serialization.DataResult;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import net.forthecrown.dungeons.level.LevelBiome;
import net.forthecrown.dungeons.level.PieceType;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.Structures;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.Results;

@Getter
public class RoomType extends PieceType<RoomPiece> {

  private final EnumSet<RoomFlag> flags;
  private final Map<LevelBiome, String> biome2Palette;

  public RoomType(
      String structureName,
      EnumSet<RoomFlag> flags,
      Map<LevelBiome, String> biome2Palette
  ) {
    super(structureName);
    this.flags = flags;
    Objects.requireNonNull(biome2Palette);

    this.biome2Palette = biome2Palette.isEmpty()
        ? Collections.emptyMap()
        : Collections.unmodifiableMap(biome2Palette);
  }

  public static DataResult<RoomType> loadType(JsonWrapper json) {
    String structName = json.getString("struct");

    if (structName == null) {
      return Results.error("No 'struct' set");
    }

    var opt = Structures.get().getRegistry().get(structName);

    if (opt.isEmpty()) {
      return Results.error("No structure named '%s'", structName);
    }
    BlockStructure structure = opt.get();

    List<RoomFlag> flags = json.getList(
        "properties",
        element -> JsonUtils.readEnum(RoomFlag.class, element)
    );

    EnumSet<RoomFlag> flagSet = EnumSet.copyOf(flags);

    Map<LevelBiome, String> palettes = new HashMap<>();
    if (json.has("palettes")) {
      var paletteObject = json.getObject("palettes");

      for (var entry: paletteObject.entrySet()) {
        LevelBiome biome = LevelBiome.valueOf(entry.getKey().toUpperCase());
        String paletteName = entry.getValue().getAsString();

        if (structure.getPalette(paletteName) == null) {
          return Results.error("No palette named '%s' in structure '%s'",
              paletteName, structName
          );
        }

        palettes.put(biome, paletteName);
      }
    }

    return DataResult.success(new RoomType(structName, flagSet, palettes));
  }

  public boolean hasFlag(RoomFlag flag) {
    return flags.contains(flag);
  }

  public String getPalette(LevelBiome biome) {
    return biome2Palette.getOrDefault(
        biome,
        BlockStructure.DEFAULT_PALETTE_NAME
    );
  }

  public EnumSet<RoomFlag> getFlags() {
    return flags.clone();
  }

  @Override
  public RoomPiece create() {
    return new RoomPiece(this);
  }

  @Override
  public RoomPiece load(CompoundTag tag) {
    return new RoomPiece(this, tag);
  }
}