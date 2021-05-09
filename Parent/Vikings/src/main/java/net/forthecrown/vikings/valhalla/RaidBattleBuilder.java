package net.forthecrown.vikings.valhalla;

import org.apache.logging.log4j.core.util.Builder;

public class RaidBattleBuilder implements Builder<RaidBattle> {

    private final VikingRaid raid;
    private final RaidParty party;

    public RaidBattleBuilder(VikingRaid raid, RaidParty party){
        this.raid = raid;
        this.party = party;
    }

    @Override
    public RaidBattle build() {
        return null;
    }
}
