package net.forthecrown.vikings.valhalla.creation;

import org.bukkit.World;
import org.bukkit.WorldCreator;

public class RaidWorldCreator {
    private final World example_world;

    public RaidWorldCreator(World example_world){
        this.example_world = example_world;
    }

    public World getExampleWorld() {
        return example_world;
    }

    public void copyWorlds(){
        WorldCreator creator = new WorldCreator("world_raids_actual")
                .generator("VoidGenerator")
                .copy(example_world);

        creator.createWorld();
    }

    public void unloadRaidWorld(){

    }
}
