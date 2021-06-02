package net.forthecrown.vikings.valhalla.data;

import net.forthecrown.emperor.serialization.JsonSerializable;
import net.forthecrown.vikings.valhalla.active.BattleBuilder;
import net.forthecrown.vikings.valhalla.active.RaidParty;

public interface RaidData extends JsonSerializable {
    void generate(RaidParty party, BattleBuilder generator);
}
