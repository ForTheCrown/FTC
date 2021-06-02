package net.forthecrown.vikings.valhalla.active;

import net.forthecrown.emperor.utils.CrownRandom;
import net.forthecrown.vikings.valhalla.VikingRaid;
import org.apache.logging.log4j.core.util.Builder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

import java.util.Objects;

public class BattleBuilder implements Builder<ActiveRaid> {

    private final RaidParty party;
    private final VikingRaid raid;

    private final TriggerContainer container;

    public CrownRandom random;

    public BattleBuilder(RaidParty party) {
        this.party = party;
        this.raid = party.raid;
        random = new CrownRandom();

        this.container = buildTriggers();
    }

    public TriggerContainer buildTriggers(){

    }

    public void createWorld(){
        World origWorld = Objects.requireNonNull(Bukkit.getWorld("world_raids"));
        WorldCreator creator = new WorldCreator("world_raids_actual")
                .copy(origWorld)
                .generator("VoidGenerator")
                .type(WorldType.FLAT)
                .environment(World.Environment.NORMAL);

        World newWorld = creator.createWorld();
    }

    @Override
    public ActiveRaid build() {
        return null;
    }
}
