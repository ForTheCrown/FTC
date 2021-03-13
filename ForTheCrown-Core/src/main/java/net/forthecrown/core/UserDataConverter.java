package net.forthecrown.core;

import net.forthecrown.core.api.Announcer;
import net.forthecrown.core.api.CrownUser;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public final class UserDataConverter {
    UserDataConverter(File oldData){
        FileConfiguration oldYaml = YamlConfiguration.loadConfiguration(oldData);
        Set<String> keys = oldYaml.getConfigurationSection("players").getKeys(false);
        if(keys.size() < 1) return;

        Announcer.log(Level.INFO, "Starting Userdata conversion");

        for (String s: keys){
            UUID id = UUID.fromString(s);
            CrownUser user = FtcCore.getUser(id);
            user.unload();
        }
        Announcer.log(Level.INFO, "userdata conversion finished");

        oldData.delete();
    }
}
