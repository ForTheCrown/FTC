package net.forthecrown.core;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

public @UtilityClass class DynmapUtil {
    public boolean isInstalled() {
        return Bukkit.getPluginManager()
                .getPlugin("dynmap") != null;
    }

    void registerListener() {
        if (!isInstalled()) {
            return;
        }

        FtcDynmap.registerListener();
    }
}