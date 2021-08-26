package net.forthecrown.core.transformers;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import net.forthecrown.core.Crown;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.utils.ListUtils;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class Regions_PopDensityToFTC {
    private static final File OLD_DIR = new File("plugins" + File.separator + "PopulationDensityData" + File.separator + "RegionData");

    public static void checkAndRun() {
        if(!OLD_DIR.exists()) return;

        File[] files = OLD_DIR.listFiles();
        if(ListUtils.isNullOrEmpty(files)) return;

        Bukkit.getScheduler().runTaskAsynchronously(Crown.inst(), () -> {
            try {
                Regions_PopDensityToFTC transformer = new Regions_PopDensityToFTC(files, Crown.getRegionManager());
                transformer.convert();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private final File[] oldFiles;
    private final RegionManager manager;
    private final Logger logger;

    private Regions_PopDensityToFTC(File[] oldFiles, RegionManager regionManager) {
        this.manager = regionManager;
        this.oldFiles = oldFiles;
        this.logger = Crown.logger();
    }

    public void convert() throws IOException {

        for (File f: oldFiles) {
            RegionPos cords = RegionPos.fromString(f.getName());
            String name = Files.readFirstLine(f, Charsets.UTF_8);

            manager.rename(manager.get(cords), name);
            logger.info("Converted " + name + " region from PopulationDensity");

            f.delete();
        }

        OLD_DIR.delete();
    }
}
