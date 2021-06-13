package net.forthecrown.vikings.valhalla.data;

import net.forthecrown.core.serializer.JsonSerializable;
import net.forthecrown.vikings.valhalla.builder.BattleBuilder;
import net.forthecrown.vikings.valhalla.active.RaidParty;

public interface RaidData extends JsonSerializable {
    void create(RaidParty party, BattleBuilder generator);
}
