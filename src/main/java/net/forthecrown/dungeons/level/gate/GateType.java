package net.forthecrown.dungeons.level.gate;

import com.google.common.base.Strings;
import java.util.Map;
import lombok.Getter;
import net.forthecrown.dungeons.level.PieceStyle;
import net.forthecrown.dungeons.level.PieceType;
import net.minecraft.nbt.CompoundTag;

@Getter
public class GateType extends PieceType<DungeonGate> {

  private final String openPalette;
  private final String closedPalette;

  public GateType(String structureName,
                  Map<PieceStyle, String> variants,
                  String openPalette,
                  String closedPalette
  ) {
    super(structureName, variants);
    this.openPalette = openPalette;
    this.closedPalette = closedPalette;
  }

  public boolean isOpenable() {
    return !Strings.isNullOrEmpty(openPalette);
  }

  @Override
  public DungeonGate create() {
    return new DungeonGate(this);
  }

  @Override
  public DungeonGate load(CompoundTag tag) {
    return new DungeonGate(this, tag);
  }
}