package net.forthecrown.core;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import org.apache.logging.log4j.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public final class FTC {
  private FTC() {}

  /**
   * Gets the FTC plugin instance
   *
   * @return FTC plugin instance
   */
  public static Main getPlugin() {
    return JavaPlugin.getPlugin(Main.class);
  }

  public static Logger getLogger() {
    return getPlugin().logger;
  }

  public static boolean inDebugMode() {
    return getPlugin().debugMode;
  }

  public static Timing timing(String name) {
    return Timings.of(getPlugin(), name);
  }
}