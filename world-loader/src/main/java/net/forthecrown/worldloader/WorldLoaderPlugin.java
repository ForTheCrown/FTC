package net.forthecrown.worldloader;

import lombok.Getter;
import net.forthecrown.BukkitServices;
import net.forthecrown.Loggers;
import net.forthecrown.command.Commands;
import net.forthecrown.grenadier.annotations.AnnotatedCommandContext;
import net.forthecrown.utils.PluginUtil;
import net.forthecrown.utils.TomlConfigs;
import net.forthecrown.worldloader.chunky.ChunkyLoaderService;
import net.forthecrown.worldloader.commands.CommandWorldLoader;
import net.forthecrown.worldloader.impl.LoaderService;
import net.forthecrown.worldloader.resetter.AutoResetManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public class WorldLoaderPlugin extends JavaPlugin {

  private static final Logger LOGGER = Loggers.getLogger();

  private static final int NONE = 0;
  private static final int CHUNKY = 1;
  private static final int IMPL = 2;

  @Getter
  private LoaderConfig loaderConfig;

  private LoaderService loader;

  private int currentLoader = NONE;

  @Getter
  private WorldRemaker remaker;

  private AutoResetManager autoResetManager;

  @Override
  public void onEnable() {
    autoResetManager = new AutoResetManager();
    remaker = new WorldRemaker();
    reloadConfig();

    AnnotatedCommandContext ctx = Commands.createAnnotationContext();
    ctx.registerCommand(new CommandWorldLoader(this));

    autoResetManager.load();
  }

  @Override
  public void onDisable() {
    loader.shutdown();
    autoResetManager.clear();
  }

  @Override
  public void reloadConfig() {
    loaderConfig = TomlConfigs.loadPluginConfig(this, LoaderConfig.class);

    if (loader == null) {
      loader = new LoaderService(this);
    }

    if (loaderConfig.useChunkyWhenAvailable && PluginUtil.isEnabled("Chunky")) {
      if (currentLoader == CHUNKY) {
        return;
      }

      if (currentLoader == IMPL) {
        BukkitServices.unregister(WorldLoaderService.class, loader);
      }

      ChunkyLoaderService.register(this);
      currentLoader = CHUNKY;

      LOGGER.debug("Set loader to chunky loader");
    } else {
      if (currentLoader == IMPL) {
        return;
      }

      if (currentLoader == CHUNKY) {
        ChunkyLoaderService.unregister();
      }

      BukkitServices.register(WorldLoaderService.class, loader);
      currentLoader = IMPL;

      LOGGER.debug("Set loader to home-written one");
    }
  }
}
