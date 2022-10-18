package net.forthecrown.dungeons.level.gate;

import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.dungeons.level.PieceTypes;
import net.forthecrown.structure.BlockStructure;

public final class Gates {
    private Gates() {}

    public static final String
            OPEN_PALETTE = BlockStructure.DEFAULT_PALETTE_NAME,
            CLOSED_DEF_PALETTE = "closed";

    public static final Registry<GateType> REGISTRY = PieceTypes.newRegistry("gates");

    public static final Holder<GateType>
            DEFAULT_GATE   = create("open",      "dungeons/gates/open_1",    true),
            COLLAPSED_GATE = create("collapsed", "dungeons/gates/collapsed", false),
            DECORATE_GATE  = create("decorate",  "dungeons/gates/decorate",  false);

    private static Holder<GateType> create(String name, String struct, boolean canOpen) {
        return REGISTRY.register(name, new GateType(struct, canOpen));
    }
}