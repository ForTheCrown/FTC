package net.forthecrown.vikings.valhalla.active;

import net.forthecrown.core.utils.CrownRandom;
import net.forthecrown.vikings.valhalla.VikingRaid;
import org.bukkit.World;

public class ActiveRaid {

    public final VikingRaid raid;
    public final RaidParty party;
    public final TriggerContainer container;
    public final RaidDifficulty difficulty;
    public final World world;

    public final CrownRandom random;

    public ActiveRaid(VikingRaid raid,
                      RaidParty party,
                      TriggerContainer container,
                      CrownRandom random,
                      RaidDifficulty difficulty,
                      World world
    ) {
        this.raid = raid;
        this.party = party;
        this.container = container;
        this.random = random;
        this.difficulty = difficulty;
        this.world = world;

        container.raid = this;
    }
}
