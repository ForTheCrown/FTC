package net.forthecrown.core;

import net.forthecrown.core.module.ModuleServices;
import net.forthecrown.core.module.OnDisable;
import net.forthecrown.core.module.OnSave;
import net.kyori.adventure.key.Namespaced;
import org.apache.logging.log4j.LogManager;
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
  FtcLogger logger;

  @Override
  public void onEnable() {
    setDebugMode();
    ensureLoggerExists();

    // Register dynmap hook connection thing
    DynmapUtil.registerListener();

    BootStrap.init();

    FTC.getLogger().info("FTC started");
  }

  @Override
  public void onLoad() {
    setDebugMode();
    FtcFlags.init();
  }

  @Override
  public void onDisable() {
    Bukkit.getScheduler()
        .cancelTasks(this);

    ModuleServices.run(OnSave.class);
    ModuleServices.run(OnDisable.class);
  }

  private void ensureLoggerExists() {
    if (logger != null) {
      return;
    }

    Bukkit.getLogger().addHandler(DiscordErrorLogHandler.INSTANCE);

    for (var p: Bukkit.getPluginManager().getPlugins()) {
      p.getLogger().addHandler(DiscordErrorLogHandler.INSTANCE);
    }

    logger = new FtcLogger(
        LogManager.getContext()
            .getLogger(getLogger().getName())
    );
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
    ensureLoggerExists();
    return logger;
  }
}