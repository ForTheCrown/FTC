package net.forthecrown.dungeons.level.gate;

import com.google.common.base.Strings;
import com.mojang.serialization.DataResult;
import lombok.Getter;
import net.forthecrown.dungeons.level.PieceType;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.Structures;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.Results;

@Getter
public class GateType extends PieceType<GatePiece> {

  private final String openPalette;
  private final String closedPalette;

  public GateType(
      String structureName,
      String openPalette,
      String closedPalette
  ) {
    super(structureName);
    this.openPalette = openPalette;
    this.closedPalette = closedPalette;
  }

  public static DataResult<GateType> loadType(JsonWrapper json) {
    String structName = json.getString("struct");

    if (structName == null) {
      return Results.error("No 'struct' set");
    }

    var opt = Structures.get().getRegistry().get(structName);

    if (opt.isEmpty()) {
      return Results.error("No structure named '%s'", structName);
    }

    JsonWrapper palettes = json.getWrapped("palettes");

    if (palettes == null) {
      return Results.error(
          "No 'palettes' key to specify 'open' and 'closed' palettes"
      );
    }

    String openPalette = palettes.getString("open", null);
    String closedPalette = palettes.getString(
        "closed",
        BlockStructure.DEFAULT_PALETTE_NAME
    );

    return DataResult.success(
        new GateType(structName, openPalette, closedPalette)
    );
  }

  public boolean isOpenable() {
    return !Strings.isNullOrEmpty(openPalette);
  }

  @Override
  public GatePiece create() {
    return new GatePiece(this);
  }

  @Override
  public GatePiece load(CompoundTag tag) {
    return new GatePiece(this, tag);
  }
}