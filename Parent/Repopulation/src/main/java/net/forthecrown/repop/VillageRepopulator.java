package net.forthecrown.repop;

import net.querz.mca.Chunk;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;

public class VillageRepopulator extends AbstractRepopulator{

    public VillageRepopulator() {
        super(Objects.requireNonNull(Bukkit.getWorld("world")));
    }

    @Override
    protected File getRegionDir() {
        return new File(world.getWorldFolder().getPath() + File.separator + "region");
    }

    @Override
    protected @Nullable PopulatorScanResult scan(Chunk chunk) {
        return null;
    }

    @Override
    protected Chunk repopulate(PopulatorScanResult result) {
        return null;
    }
}
