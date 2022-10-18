package net.forthecrown.dungeons.level.gate;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.dungeons.level.DungeonPiece;
import net.forthecrown.dungeons.level.DungeonRoom;
import net.forthecrown.dungeons.level.PieceVisitor;
import net.minecraft.nbt.CompoundTag;

@Getter @Setter
public class DungeonGate extends DungeonPiece {
    private static final String
            TAG_ORIGIN = "origin_gate",
            TAG_TARGET = "target_gate",
            TAG_PARENT_EXIT = "parent_exit_gate",
            TAG_OPEN = "open";

    boolean open = true;

    private AbsoluteGateData parentExit;
    private AbsoluteGateData originGate;
    private AbsoluteGateData targetGate;

    public DungeonGate(GateType type) {
        super(type);
    }

    public DungeonGate(GateType type, CompoundTag tag) {
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
        setPaletteName(!open ? Gates.CLOSED_DEF_PALETTE : Gates.OPEN_PALETTE);
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
        return o instanceof DungeonRoom && this.isOpen();
    }

    @Override
    protected PieceVisitor.Result onVisit(PieceVisitor walker) {
        return walker.onGate(this);
    }
}