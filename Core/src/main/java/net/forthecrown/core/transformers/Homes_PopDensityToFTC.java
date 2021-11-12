package net.forthecrown.core.transformers;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import net.forthecrown.core.Crown;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.user.FtcUserHomes;
import net.forthecrown.utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

public class Homes_PopDensityToFTC implements Runnable {
    private static final File PLAYER_DATA_DIR = new File("plugins/PopulationDensityData/PlayerData");

    public static void checkAndRun() {
        if(!PLAYER_DATA_DIR.exists() || !PLAYER_DATA_DIR.isDirectory()) return;

        File[] files = PLAYER_DATA_DIR.listFiles();
        if(files.length < 1) return;

        Homes_PopDensityToFTC converter = new Homes_PopDensityToFTC(files);
        converter.run();
    }

    private final File[] files;
    private final Logger logger;

    public Homes_PopDensityToFTC(File[] files) {
        this.files = files;
        this.logger = Crown.logger();
    }

    @Override
    public void run() {
        for (File f: files) {
            try {
                UUID uuid = UUID.fromString(f.getName());
                String homePos = Files.readFirstLine(f, Charsets.UTF_8);
                RegionPos pos = RegionPos.fromString(homePos);
                if(pos.getX() == 0 && pos.getZ() == 0) continue;

                File userFile = Crown.getUserManager().getSerializer().getFile(uuid);
                if(!userFile.exists()) {
                    f.delete();
                    continue;
                }

                JsonObject userJson = JsonUtils.readFile(userFile);
                JsonObject homes = userJson.getAsJsonObject("homes");
                if(homes == null) homes = new JsonObject();

                homes.addProperty(FtcUserHomes.HOME_REGION_JSON_NAME, pos.toString());
                JsonUtils.writeFile(userJson, userFile);

                logger.info("Converted homes of " + uuid + " from PopDensity to FTC");
            } catch (IOException e) {
                logger.severe("Error converting homes of " + f.getName());

                e.printStackTrace();
                return;
            }
        }

        logger.info("Converted all PopulationDensity homes to FTC");
        PLAYER_DATA_DIR.renameTo(new File("plugins/PopulationDensityData/PlayerData_backup"));
    }
}