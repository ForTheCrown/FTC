package net.forthecrown.core;

import org.bukkit.Bukkit;

public final class DynmapUtil {
  private DynmapUtil() {}

  public static boolean isInstalled() {
    return Bukkit.getPluginManager()
        .getPlugin("dynmap") != null;
  }

  static void registerListener() {
    if (!isInstalled()) {
      return;
    }

    FtcDynmap.registerListener();
  }
}