package net.forthecrown.vikings.valhalla.active;

import net.forthecrown.emperor.utils.CrownRandom;
import net.forthecrown.vikings.valhalla.VikingRaid;

public class ActiveRaid {

    private final VikingRaid raid;
    private final RaidParty party;
    private final TriggerContainer container;
    private final RaidDifficulty difficulty;

    private final CrownRandom random;

    public ActiveRaid(VikingRaid raid,
                      RaidParty party,
                      TriggerContainer container,
                      CrownRandom random,
                      RaidDifficulty difficulty
    ) {
        this.raid = raid;
        this.party = party;
        this.container = container;
        this.random = random;
        this.difficulty = difficulty;

        container.raid = this;
    }

    public VikingRaid getRaid() {
        return raid;
    }

    public RaidParty getParty() {
        return party;
    }

    public TriggerContainer getContainer() {
        return container;
    }

    public RaidDifficulty getDifficulty() {
        return difficulty;
    }

    public CrownRandom getRandom() {
        return random;
    }
}
