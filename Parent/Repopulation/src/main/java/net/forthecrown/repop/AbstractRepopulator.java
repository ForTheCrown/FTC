package net.forthecrown.repop;

import net.querz.mca.Chunk;
import net.querz.mca.LoadFlags;
import net.querz.mca.MCAFile;
import net.querz.mca.MCAUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public abstract class AbstractRepopulator {

    protected final World world;

    public AbstractRepopulator(World world) {
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    public void initiate(Logger logger) throws IOException {
        Validate.isTrue(Bukkit.getOnlinePlayers().size() < 1, "Server must be empty to initiate repopulator");

        logger.info("Beginning repopulator on " + world.getName());
        File anvilDir = getRegionDir();
        logger.info("1");

        for (File f: anvilDir.listFiles()){
            logger.info("2");

            MCAFile mca = MCAUtil.read(f, LoadFlags.STRUCTURES);
            logger.info(mca.toString());

            for (int i = 0; i < 1024; i++){
                try {
                    logger.info("Checking chunk, index: " + i);
                    Chunk c = mca.getChunk(i);
                    if(c == null) continue;

                    PopulatorScanResult scanResult = scan(c);
                    if(scanResult == null) continue;

                    Chunk newChunk = repopulate(scanResult);
                    mca.setChunk(i, newChunk);
                    logger.info("Repopulated chunk, index: " + i);
                } catch (IndexOutOfBoundsException e){
                    continue;
                }
            }

            MCAUtil.write(mca, f);
        }
        logger.info("Repopulator finished");
    }

    protected abstract File getRegionDir();
    protected abstract @Nullable PopulatorScanResult scan(Chunk chunk);
    protected abstract @Nullable Chunk repopulate(PopulatorScanResult result);
}
