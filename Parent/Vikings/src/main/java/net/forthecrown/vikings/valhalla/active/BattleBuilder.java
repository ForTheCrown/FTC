package net.forthecrown.vikings.valhalla.active;

import net.forthecrown.emperor.utils.CrownRandom;
import net.forthecrown.vikings.valhalla.VikingRaid;
import net.forthecrown.vikings.valhalla.data.RaidGenerationData;
import net.forthecrown.vikings.valhalla.data.TriggerData;
import org.apache.logging.log4j.core.util.Builder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

import java.util.Objects;

public class BattleBuilder implements Builder<ActiveRaid> {

    private final RaidParty party;
    private final VikingRaid raid;
    private final RaidGenerationData data;
    private final TriggerContainer container;
    private final RaidDifficulty difficulty;
    private final World world;

    public CrownRandom random;

    public BattleBuilder(RaidParty party) {
        this.party = party;
        this.raid = party.raid;
        this.data = raid.generatorData;
        random = new CrownRandom();

        this.container = buildTriggers();
        this.difficulty = buildDifficulty();
        this.world = buildWorld();

        buildWorld();
    }

    public RaidDifficulty buildDifficulty(){
        return new RaidDifficulty();
    }

    public TriggerContainer buildTriggers(){
        TriggerData triggers = data.triggerData;
        if(triggers == null || triggers.triggers.size() < 1) return null;
    }

    public World buildWorld(){
        World origWorld = Objects.requireNonNull(Bukkit.getWorld("world_raids"));
        WorldCreator creator = new WorldCreator("world_raids_actual")
                .copy(origWorld)
                .generator("VoidGenerator")
                .type(WorldType.FLAT)
                .environment(World.Environment.NORMAL);

        return creator.createWorld();
    }

    @Override
    public ActiveRaid build() {
        return new ActiveRaid(raid, party, container, random, difficulty, world);
    }
}
