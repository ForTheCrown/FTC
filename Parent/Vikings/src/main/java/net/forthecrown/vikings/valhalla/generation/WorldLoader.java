package net.forthecrown.vikings.valhalla.generation;

import net.forthecrown.vikings.Vikings;
import org.bukkit.*;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class WorldLoader {

    private final Server server;
    private final Vikings vikings;
    private World raidWorld;

    public static final World WORLD_RAID_EXAMPLES = Objects.requireNonNull(Bukkit.getWorld("world_raids"));

    public WorldLoader(Server server, Vikings vikings){
        this.server = server;
        this.vikings = vikings;
    }

    public void unloadRaidWorld(){
        if(raidWorld == null) return;
        server.unloadWorld(raidWorld, true);
    }

    public void createWorld(){
        WorldCreator creator = new WorldCreator("world_raids_actual")
                .copy(WORLD_RAID_EXAMPLES)
                .generator("VoidGenerator")
                .environment(World.Environment.NORMAL);

        raidWorld = creator.createWorld();
    }

    public @Nullable World getRaidWorld() {
        return raidWorld;
    }
}
