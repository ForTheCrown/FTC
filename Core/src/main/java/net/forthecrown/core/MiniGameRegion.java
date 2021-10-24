package net.forthecrown.core;

import static net.forthecrown.core.ComVars.*;

public interface MiniGameRegion {
    static int chickenLevitation() { return chickenLevitation.getValue(5); }
    static int chickenLevitationTime() { return chickenLevitationTime.getValue(10); }
}