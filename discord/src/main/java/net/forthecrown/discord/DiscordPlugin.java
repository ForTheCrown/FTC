package net.forthecrown.discord;

import static net.forthecrown.discord.DiscordAppender.APPENDER_NAME;

import lombok.Getter;
import net.forthecrown.discord.commands.AppenderCommand;
import net.forthecrown.discord.listener.ServerLoadListener;
import net.forthecrown.events.Events;
import net.forthecrown.utils.TomlConfigs;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscordPlugin extends JavaPlugin {

  @Getter
  private Config pluginConfig = new Config();

  @Override
  public void onEnable() {
    reloadConfig();

    new AppenderCommand();
    Events.register(new ServerLoadListener());
  }

  @Override
  public void reloadConfig() {
    pluginConfig = TomlConfigs.loadPluginConfig(this, Config.class);
    updateLoggers(false);
  }

  @Override
  public void onDisable() {
    updateLoggers(true);
  }

  void updateLoggers(boolean remove) {
    var ctx = LoggerContext.getContext(false);
    var config = ctx.getConfiguration();
    var root = config.getRootLogger();

    root.removeAppender(APPENDER_NAME);

    if (!remove) {
      root.addAppender(new DiscordAppender(config.getName()), getAppenderLevel(), null);
    }

    LoggerConfig discordSRV = new LoggerConfig("DiscordSRV", Level.OFF, false);
    config.addLogger("DiscordSRV", discordSRV);

    ctx.updateLoggers();
  }

  Level getAppenderLevel() {
    String name = pluginConfig.getForwarderLevel();
    return Level.toLevel(name, Level.ERROR);
  }
}