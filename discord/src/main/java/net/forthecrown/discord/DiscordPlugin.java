package net.forthecrown.discord;

import static net.forthecrown.discord.DiscordAppender.APPENDER_NAME;

import lombok.Getter;
import net.forthecrown.utils.TomlConfigs;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscordPlugin extends JavaPlugin {

  @Getter
  private Config config = new Config();

  @Override
  public void onEnable() {
    reloadConfig();
    new AppenderCommand();
  }

  @Override
  public void reloadConfig() {
    config = TomlConfigs.loadPluginConfig(this, Config.class);
    updateLoggers();
  }

  @Override
  public void onDisable() {

  }

  void updateLoggers() {
    var ctx = LoggerContext.getContext(false);
    var config = ctx.getConfiguration();
    var root = config.getRootLogger();

    root.removeAppender(APPENDER_NAME);
    root.addAppender(new DiscordAppender(config.getName()), getAppenderLevel(), null);

    LoggerConfig discordSRV = new LoggerConfig("DiscordSRV", Level.OFF, false);
    config.addLogger("DiscordSRV", discordSRV);

    ctx.updateLoggers();
  }

  Level getAppenderLevel() {
    String name = config.getForwarderLevel();
    return Level.toLevel(name, Level.ERROR);
  }
}