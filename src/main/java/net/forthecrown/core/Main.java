package net.forthecrown.core;

import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.module.ModuleServices;
import net.forthecrown.dungeons.enchantments.FtcEnchants;
import net.kyori.adventure.key.Namespaced;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public final class Main extends JavaPlugin implements Namespaced {

  public static final String
      NAME = "ForTheCrown",
      NAMESPACE = NAME.toLowerCase(),
      OLD_NAMESPACE = "ftccore";

  boolean debugMode;

  @Override
  public void onEnable() {
    setDebugMode();

    // Register dynmap hook connection thing
    DynmapUtil.registerListener();
    BootStrap.init();

    getLog4JLogger().info("FTC started");
  }

  @Override
  public void onLoad() {
    FtcFlags.init();
    FtcEnchants.init();
  }

  @Override
  public void onDisable() {
    Bukkit.getScheduler()
        .cancelTasks(this);

    ModuleServices.SAVE.run();
    ModuleServices.ON_DISABLE.run();
  }

  private void setDebugMode() {
    YamlConfiguration config = YamlConfiguration.loadConfiguration(
        getTextResource("plugin.yml")
    );

    debugMode = config.getBoolean("debug_build");
  }

  @Override
  public @NonNull String namespace() {
    return NAMESPACE;
  }

  @Override
  public @NotNull Logger getLog4JLogger() {
    return Loggers.getPluginLogger();
  }
}