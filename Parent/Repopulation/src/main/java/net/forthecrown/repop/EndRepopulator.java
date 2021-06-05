package net.forthecrown.repop;

import net.querz.mca.Chunk;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class EndRepopulator extends AbstractRepopulator {
    public EndRepopulator() {
        super(Objects.requireNonNull(Bukkit.getWorld("world_the_end")));
    }

    @Override
    protected File getRegionDir() {
        return new File(world.getWorldFolder().getPath() + File.separator + "DIM1" + File.separator + "region");
    }

    @Override
    protected @Nullable PopulatorScanResult scan(Chunk chunk) {
        if(!chunk.getStructures().containsKey("endcity")) return null;
        if(chunk.getEntities().size() > 3) return null;

        CompoundTag tag = chunk.getStructures();
        CompoundTag starts = tag.getCompoundTag("Starts");
        CompoundTag endCity = starts.getCompoundTag("EndCity");

        if(endCity.getString("ID").equals("INVALID")){
            Repopulation.logger.info("Was invalid");
            return null;
        }

        return new PopulatorScanResult(new ArrayList<>(), chunk.getStructures(), tag, chunk);
    }

    @Override
    protected Chunk repopulate(PopulatorScanResult result) {
        return null;
    }

    /*private CompoundTag createShulker(BlockPos loc){
        CompoundTag tag = PopUtil.entitySpawnHelp(true, false);

        return tag;
    }*/
}
