package net.forthecrown.squire.repopulation;

import net.querz.mca.Chunk;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;

public class EndRepopulator extends AbstractRepopulator implements Repopulator {

    @Override
    public ChunkScan scan(Chunk chunk) {
        return ChunkScan.noFixNeeded();
    }

    @Override
    public Chunk repopulate(ChunkScan scan) {
        return null;
    }

    @Override
    public File getRegionDirectory() {
        return new File(getWorld().getWorldFolder().getPath() + "DIM1" + File.separator + "region");
    }

    @Override
    public World getWorld() {
        return Bukkit.getWorld("world_the_end");
    }
}
