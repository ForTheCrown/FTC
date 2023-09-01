package net.forthecrown.guilds;

import lombok.Getter;
import net.forthecrown.FtcServer;
import net.forthecrown.guilds.commands.GuildCommands;
import net.forthecrown.guilds.listeners.GuildEvents;
import net.forthecrown.guilds.unlockables.Unlockables;
import net.forthecrown.guilds.waypoints.GuildWaypoints;
import net.forthecrown.utils.TomlConfigs;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class GuildPlugin extends JavaPlugin {

  private GuildManager manager;
  private GuildConfig guildConfig;

  public static GuildPlugin get() {
    return JavaPlugin.getPlugin(GuildPlugin.class);
  }

  @Override
  public void onEnable() {
    manager = new GuildManager(this);
    reloadConfig();

    FtcServer server = FtcServer.server();
    GUserProperties.init(server.getGlobalSettingsBook());

    Unlockables.registerAll();
    GuildEvents.registerAll(this);
    GuildCommands.createCommands();
    GuildWaypoints.init(manager);

    manager.load();
  }

  @Override
  public void onDisable() {
    GuildWaypoints.close();
  }

  @Override
  public void reloadConfig() {
    guildConfig = TomlConfigs.loadPluginConfig(this, GuildConfig.class);
  }
}
