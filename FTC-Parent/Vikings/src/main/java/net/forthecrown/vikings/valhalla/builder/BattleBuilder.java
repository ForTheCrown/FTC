package net.forthecrown.vikings.valhalla.builder;

import net.forthecrown.core.utils.CrownRandom;
import net.forthecrown.vikings.valhalla.VikingRaid;
import net.forthecrown.vikings.valhalla.active.*;
import net.forthecrown.vikings.valhalla.data.RaidGenerationData;
import net.forthecrown.vikings.valhalla.data.TriggerData;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.core.util.Builder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.util.BoundingBox;

import java.util.Objects;

public class BattleBuilder implements Builder<ActiveRaid> {

    public final RaidParty party;
    public final VikingRaid raid;
    public final RaidGenerationData data;
    public final TriggerContainer container;
    public final RaidDifficulty difficulty;
    public final World world;

    public CrownRandom random;

    public BattleBuilder(RaidParty party) {
        this.party = party;
        this.raid = party.raid;
        this.data = raid.generatorData;
        random = new CrownRandom();

        this.difficulty = buildDifficulty();
        this.world = buildWorld();

        this.container = buildTriggers();

        buildWorld();
        buildData();
    }

    public RaidDifficulty buildDifficulty(){
        return new RaidDifficulty(this);
    }

    public TriggerContainer buildTriggers(){
        TriggerData triggers = data.triggerData;
        if(triggers == null || triggers.triggers.size() < 1) return null;

        BoundingBox box = raid.region;
        Validate.isTrue(box.getWidthX() >= 32, "Viking region cannot be less than 32 blocks in width");
        Validate.isTrue(box.getWidthZ() >= 32, "Viking region cannot be less than 32 blocks in width");

        int cellSizeX = (int) (box.getWidthX() / 16);
        int cellSizeZ = (int) (box.getWidthZ() / 16);

        RaidCell[] cells = new RaidCell[256];

        for (int i = 0; i < 16; i++){
            for (int j = 0; j < 16; j++){
                cells[i + j] = new RaidCell(i, j);
            }
        }

        return new TriggerContainer(
                cellSizeX,
                cellSizeZ,

        );
    }

    public World buildWorld(){
        World raidWorld = Bukkit.getWorld("world_raids_actual");
        if(raidWorld != null) Bukkit.unloadWorld(raidWorld, false);

        World origWorld = Objects.requireNonNull(Bukkit.getWorld("world_raids"));
        WorldCreator creator = new WorldCreator("world_raids_actual").copy(origWorld);

        return creator.createWorld();
    }

    public void buildData(){
        RaidGenerationData genData = raid.generatorData;
        if(genData == null) return;

        if(genData.worldData != null) genData.worldData.create(party, this);
        if(genData.lootData != null) genData.lootData.create(party, this);
        if(genData.mobData != null) genData.mobData.create(party, this);
    }

    @Override
    public ActiveRaid build() {
        return new ActiveRaid(raid, party, container, random, difficulty, world);
    }
}
