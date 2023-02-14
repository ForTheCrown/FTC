package net.forthecrown.dungeons.level.gate;

import com.google.common.base.Strings;
import lombok.Getter;
import net.forthecrown.dungeons.level.PieceType;
import net.minecraft.nbt.CompoundTag;

@Getter
public class GateType extends PieceType<GatePiece> {

  private final String openPalette;
  private final String closedPalette;

  public GateType(String structureName,
                  String openPalette,
                  String closedPalette
  ) {
    super(structureName);
    this.openPalette = openPalette;
    this.closedPalette = closedPalette;
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