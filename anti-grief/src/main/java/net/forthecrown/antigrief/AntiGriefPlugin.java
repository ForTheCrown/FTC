package net.forthecrown.antigrief;

import lombok.Getter;
import net.forthecrown.antigrief.commands.AntiGriefCommands;
import net.forthecrown.antigrief.listeners.AntiGriefListeners;
import net.forthecrown.utils.PeriodicalSaver;
import net.forthecrown.utils.TomlConfigs;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class AntiGriefPlugin extends JavaPlugin {

  private AntiGriefConfig config = new AntiGriefConfig();
  private PeriodicalSaver saver;

  @Override
  public void onEnable() {
    saver = PeriodicalSaver.create(this::save, config::getAutosaveInterval);
    saver.start();

    Punishments.get().load();

    AntiGriefCommands.createCommands();
    AntiGriefListeners.registerAll();
  }

  @Override
  public void reloadConfig() {
    config = TomlConfigs.loadPluginConfig(this, AntiGriefConfig.class);
  }

  @Override
  public void onDisable() {
    save();
    saver.stop();
  }

  void save() {
    Punishments.get().save();
  }
}