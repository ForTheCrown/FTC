package net.forthecrown.structure;

import net.forthecrown.math.Rot;
import net.forthecrown.math.Vec2i;

public record Connector(Vec2i pos, Rot rot) {
    public static final Connector NULL = new Connector(Vec2i.ZERO, Rot.D_0);
}
