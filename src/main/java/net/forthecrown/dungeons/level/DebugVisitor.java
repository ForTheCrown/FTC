package net.forthecrown.dungeons.level;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.dungeons.level.gate.DungeonGate;

import static org.apache.commons.lang3.CharUtils.LF;

@Getter
@RequiredArgsConstructor
public class DebugVisitor implements PieceVisitor {
    private static final int INDENT_CHANGE = 2;

    private final StringBuffer buffer;
    private int indent;

    @Override
    public Result onGate(DungeonGate gate) {
        field("Type", "GATE");

        field("Open", gate.isOpen());

        field("Origin", gate.getOriginGate());
        field("Target", gate.getTargetGate());

        append(gate);
        return Result.CONTINUE;
    }

    @Override
    public Result onRoom(DungeonRoom room) {
        field("Type", "ROOM");
        append(room);
        return Result.CONTINUE;
    }

    private void append(DungeonPiece piece) {
        field("Rotation", piece.getRotation());
        field("Depth", piece.getDepth());
        field("Bounds", piece.getBounds());
        field("Placed", piece.isPlaced());
        field("Pivot Position", piece.getPivotPosition());
        field("Palette", piece.getPaletteName());
        field("Structure", piece.getType().getStructureName());
        field("ID", piece.getId());
    }

    @Override
    public void onChildrenStart(DungeonPiece piece) {
        field("Children", "[");
        addIndent();
    }

    @Override
    public void onChildrenEnd(DungeonPiece piece) {
        subIndent();

        nlIndent();
        buffer.append("]");
    }

    @Override
    public void onPieceStart(DungeonPiece piece) {
        nlIndent();
        buffer.append("{");

        addIndent();
    }

    @Override
    public void onPieceEnd(DungeonPiece piece) {
        subIndent();

        nlIndent();
        buffer.append("}");
    }

    /* ----------------------------- INTERNAL ------------------------------ */

    void addIndent() {
        indent += INDENT_CHANGE;
    }

    void subIndent() {
        indent -= INDENT_CHANGE;
    }

    // New line and indent
    void nlIndent() {
        buffer
                .append(LF)
                .append(" ".repeat(indent));
    }

    void field(String f, Object val) {
        if (val == null) {
            return;
        }

        nlIndent();

        buffer.append(f)
                .append(": ")
                .append(val);
    }
}