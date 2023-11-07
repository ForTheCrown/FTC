package net.forthecrown.leaderboards;

import lombok.Getter;
import net.forthecrown.BukkitServices;
import net.forthecrown.events.Events;
import net.forthecrown.leaderboards.commands.LeaderboardCommands;
import net.forthecrown.leaderboards.listeners.PlayerListener;
import net.forthecrown.leaderboards.listeners.ServerListener;
import net.forthecrown.utils.PeriodicalSaver;
import net.forthecrown.utils.TomlConfigs;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class LeaderboardPlugin extends JavaPlugin {

  private BoardsConfig boardsConfig;
  private ServiceImpl service;

  private PeriodicalSaver saver;

  static LeaderboardPlugin plugin() {
    return JavaPlugin.getPlugin(LeaderboardPlugin.class);
  }

  @Override
  public void onEnable() {
    service = new ServiceImpl(this);
    saver = PeriodicalSaver.create(service::save, () -> boardsConfig.autosaveInterval());

    BukkitServices.register(LeaderboardService.class, service);
    service.getTriggers().activate();
    service.createDefaultSources();

    Events.register(new ServerListener(this));
    Events.register(new PlayerListener(this));

    LeaderboardCommands.createCommands(this);
  }

  @Override
  public void onDisable() {
    service.getTriggers().close();
  }

  @Override
  public void reloadConfig() {
    this.boardsConfig = TomlConfigs.loadPluginConfig(this, BoardsConfig.class);
    saver.start();
  }

  public void reload() {
    reloadConfig();
    service.load();
  }
}
