package net.forthecrown.emperor.datafixers;

import net.forthecrown.emperor.user.FtcUser;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EssToFTC {
    private static final File ESS_USER_DIR = new File("plugins" + File.separator + "Essentials" + File.separator + "userdata");

    public static boolean hasEssData(FtcUser user){
        if(!ESS_USER_DIR.exists()) return false;
        if(!ESS_USER_DIR.isDirectory()) return false;

        File file = new File(ESS_USER_DIR, user.getUniqueId().toString() + ".yml");
        if(!file.exists()) return false;

        return file.length() >= 1;
    }

    public static EssToFTC of(FtcUser user){
        return new EssToFTC(user);
    }

    private final YamlConfiguration config;
    private final File file;

    private final FtcUser user;
    public EssToFTC(FtcUser user) {
        this.user = user;
        file = new File(ESS_USER_DIR, user.getUniqueId().toString() + ".yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void convert(){
        convertIP();
        convertHomes();
        convertNick();
    }

    public void complete(){
        file.renameTo(new File(ESS_USER_DIR, "converted_" + user.getUniqueId().toString() + ".yml"));
        user.save();
    }

    public void convertIP(){
        String s = config.getString("ipAddress");
        if(s == null) return;

        user.ip = s;
    }

    public void convertNick(){
        String s = config.getString("nickname");
        if(s == null) return;

        user.setNickname(s);
    }

    public void convertHomes(){
        ConfigurationSection homes = config.getConfigurationSection("homes");
        if(homes == null) return;

        Map<String, Location> essHomes = new HashMap<>();
        for (String s: homes.getKeys(false)){
            essHomes.put(s, Location.deserialize(homes.getConfigurationSection(s).getValues(false)));
        }

        user.homes.homes.putAll(essHomes);
    }
}
