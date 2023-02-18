package net.forthecrown.dungeons.level.gate;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.dungeons.level.DungeonPiece;
import net.forthecrown.dungeons.level.PieceVisitor;
import net.forthecrown.dungeons.level.room.RoomPiece;
import net.minecraft.nbt.CompoundTag;

@Getter
@Setter
public class GatePiece extends DungeonPiece {

  private static final String
      TAG_ORIGIN = "origin_gate",
      TAG_TARGET = "target_gate",
      TAG_PARENT_EXIT = "parent_exit_gate",
      TAG_OPEN = "open";

  boolean open = true;

  private AbsoluteGateData parentExit;
  private AbsoluteGateData originGate;
  private AbsoluteGateData targetGate;

  public GatePiece(GateType type) {
    super(type);
  }

  public GatePiece(GateType type, CompoundTag tag) {
    super(type, tag);

    if (tag.contains(TAG_OPEN) && type.isOpenable()) {
      setOpen(!tag.getBoolean(TAG_OPEN));
    }

    setOriginGate(AbsoluteGateData.load(tag.get(TAG_ORIGIN)));
    setTargetGate(AbsoluteGateData.load(tag.get(TAG_TARGET)));
    setParentExit(AbsoluteGateData.load(tag.get(TAG_PARENT_EXIT)));
  }

  @Override
  public GateType getType() {
    return (GateType) super.getType();
  }

  public void setOpen(boolean open) {
    if (!getType().isOpenable()) {
      return;
    }

    this.open = open;
  }

  @Override
  public String getPaletteName() {
    if (!getType().isOpenable()) {
      return getType().getClosedPalette();
    }

    return isOpen()
        ? getType().getOpenPalette()
        : getType().getClosedPalette();
  }

  public boolean isOpen() {
    return open && getType().isOpenable();
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    if (getType().isOpenable()) {
      tag.putBoolean(TAG_OPEN, open);
    }

    if (originGate != null) {
      tag.put(TAG_ORIGIN, originGate.save());
    }

    if (targetGate != null) {
      tag.put(TAG_TARGET, targetGate.save());
    }

    if (parentExit != null) {
      tag.put(TAG_PARENT_EXIT, parentExit.save());
    }
  }

  @Override
  protected boolean canBeChild(DungeonPiece o) {
    return o instanceof RoomPiece
        && this.isOpen()
        && !hasChildren();
  }

  @Override
  protected PieceVisitor.Result onVisit(PieceVisitor walker) {
    return walker.onGate(this);
  }
}