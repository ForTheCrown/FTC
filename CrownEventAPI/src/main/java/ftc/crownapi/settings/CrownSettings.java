package ftc.crownapi.settings;

import ftc.crownapi.Main;
import org.bukkit.configuration.ConfigurationSection;

public class CrownSettings {

    private static final ConfigurationSection settingSection = Main.plugin.getConfig().getConfigurationSection("settings");

    public static ConfigurationSection getSettingSection() {
        return settingSection;
    }

    public static boolean getSetting(CrownBooleanSettings setting){
        String stringSetting = setting.toString().replace('_', '-');
        return settingSection.getBoolean(stringSetting.toLowerCase());
    }
    public static void setSetting(CrownBooleanSettings setting, boolean value){
        String stringSetting = setting.toString().replace('_', '-');
        settingSection.set(stringSetting.toLowerCase(), value);
    }


    public static void reloadSettings() {
        Main.plugin.reloadConfig();
    }
    public static void saveSettings() {
        Main.plugin.saveConfig();
    }
}