package net.forthecrown.datafix;

import net.forthecrown.utils.io.PathUtil;

import java.nio.file.Path;

import static net.forthecrown.utils.io.PathUtil.safeDelete;

public class PluginDirectoryCleanup extends DataUpdater {
    protected boolean update() {
        Path pluginDir = PathUtil.getPluginDirectory();

        safeDelete(pluginDir.resolve("battle_pass.json"));
        safeDelete(pluginDir.resolve("comvars.json"));
        safeDelete(pluginDir.resolve("cache_converted"));
        safeDelete(pluginDir.resolve("dungeon_levels.dat"));
        safeDelete(pluginDir.resolve("shopList.txt"));
        safeDelete(pluginDir.resolve("legacy_data.json"));
        safeDelete(pluginDir.resolve("jails.json"));
        safeDelete(pluginDir.resolve("item_prices.json"));
        safeDelete(pluginDir.resolve("houses.json"));
        safeDelete(pluginDir.resolve("holidays.dat"));
        safeDelete(pluginDir.resolve("structures.dat"));
        safeDelete(pluginDir.resolve("guild.json"));

        safeDelete(pluginDir.resolve("translations"), true);
        safeDelete(pluginDir.resolve("shops"), true);

        return true;
    }
}