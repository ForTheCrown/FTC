package net.forthecrown.squire.repopulation;

import net.querz.mca.Chunk;
import net.querz.mca.MCAFile;
import net.querz.mca.MCAUtil;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public abstract class AbstractRepopulator implements Repopulator {

    @Override
    public void run(Logger logger) {
        for (File f: getRegionDirectory().listFiles()){
            try {
                MCAFile region = MCAUtil.read(f);

                for (int i = 0; i < Repopulators.MAX_CHUNKS; i++){
                    try {
                        Chunk chunk = region.getChunk(i);

                        ChunkScan scan = scan(chunk);
                        if(scan == null || !scan.needsRepopulation) continue;

                        region.setChunk(i, repopulate(scan));
                    } catch (IndexOutOfBoundsException ignored){ }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
