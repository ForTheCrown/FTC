package net.forthecrown.core;

import net.forthecrown.core.api.Announcer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

//TODO: Move into CrownUserManager
public final class FileChecker {

    private final File dataFolder;
    public FileChecker(File dataFolder){
        this.dataFolder = dataFolder;
        checkAllUserDatas();
    }

    private void log(String s){
        Announcer.log(Level.INFO, s);
    }

    private void checkAllUserDatas(){
        File file = new File(dataFolder.getPath() + File.separator + "playerdata");
        if(!file.isDirectory()) return;

        int amount = 0;
        for (File f: file.listFiles()){
            FileConfiguration config = YamlConfiguration.loadConfiguration(f);
            if(config.getLong("TimeStamps.LastLoad") == 0){

                config.set("TimeStamps.LastLoad", System.currentTimeMillis());
                try {
                    config.save(f);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                continue;
            }

            if((System.currentTimeMillis() - config.getLong("TimeStamps.LastLoad") > FtcCore.getUserDataResetInterval())){
                if(!f.delete()) FtcCore.getInstance().getLogger().log(Level.WARNING, "Couldn't delete file named " + f.getName());
                else log("Deleted file of user " + f.getName() + ". File was last loaded more than 2 months ago");
                resetBalance(f.getName());

                amount++;
            }
        }
        log("All user data files have been checked for deletion. Deleted " + amount + " files.");
    }

    private void resetBalance(String toUUID){
        UUID id = UUID.fromString(toUUID);
        Map<UUID, Integer> map = FtcCore.getBalances().getBalanceMap();
        map.remove(id);
        FtcCore.getBalances().setBalanceMap(map);
    }
}