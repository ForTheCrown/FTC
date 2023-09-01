package net.forthecrown.guilds.util;

import net.forthecrown.utils.PluginUtil;

public class DynmapUtil {

  public static boolean isInstalled() {
    return PluginUtil.isEnabled("dynmap");
  }

}
