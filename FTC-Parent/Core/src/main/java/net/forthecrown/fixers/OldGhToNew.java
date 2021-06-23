package net.forthecrown.datafixers;

import net.forthecrown.core.CrownCore;
import net.forthecrown.pirates.grappling.GhLevelEndData;
import net.forthecrown.utils.BlockPos;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class OldGhToNew {

    private final YamlConfiguration oldYaml;
    private final File file;

    private final Logger logger;

    public OldGhToNew() {
        this.logger = CrownCore.logger();

        file = new File("plugins/Pirate/TargetStandData.yml");
        oldYaml = YamlConfiguration.loadConfiguration(file);
    }

    public Map<String, GhLevelEndData> convert(){
        Map<String, GhLevelEndData> result = new HashMap<>();

        for (String s: oldYaml.getKeys(false)){
            ConfigurationSection section = oldYaml.getConfigurationSection(s);

            GhLevelEndData.Type type = GhLevelEndData.Type.byId(section.getInt("StandClass"));
            BlockPos pos = new BlockPos(section.getInt("XToCords"), section.getInt("YToCords"), section.getInt("ZToCords"));
            byte nextHooks = (byte) section.getInt("NextLevelHooks", -1);
            byte nextDistance = (byte) section.getInt("NextLevelDistance", -1);

            GhLevelEndData data = new GhLevelEndData(type, pos, nextHooks, nextDistance);
            result.put(s.toLowerCase(), data);
        }

        file.delete();
        return result;
    }
}
