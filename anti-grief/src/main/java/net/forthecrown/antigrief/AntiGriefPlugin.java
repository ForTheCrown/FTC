package net.forthecrown.antigrief;

import lombok.Getter;
import net.forthecrown.FtcServer;
import net.forthecrown.antigrief.commands.AntiGriefCommands;
import net.forthecrown.antigrief.listeners.AntiGriefListeners;
import net.forthecrown.command.settings.SettingsBook;
import net.forthecrown.user.User;
import net.forthecrown.utils.PeriodicalSaver;
import net.forthecrown.utils.TomlConfigs;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class AntiGriefPlugin extends JavaPlugin {

  private AntiGriefConfig pluginConfig = new AntiGriefConfig();
  private PeriodicalSaver saver;

  @Override
  public void onEnable() {
    reloadConfig();

    saver = PeriodicalSaver.create(this::save, () -> pluginConfig.getAutosaveInterval());
    saver.start();

    Punishments.get().load();

    AntiGriefCommands.createCommands();
    AntiGriefListeners.registerAll(this);

    FtcServer server = FtcServer.server();
    SettingsBook<User> settingsBook = server.getGlobalSettingsBook();

    EavesDropper.createSettings(settingsBook);
    StaffChat.createSettings(settingsBook);
    StaffNote.createSettings(settingsBook);
  }

  @Override
  public void reloadConfig() {
    pluginConfig = TomlConfigs.loadPluginConfig(this, AntiGriefConfig.class);
    BannedWords.load();
  }

  public void reload() {
    reloadConfig();
    Punishments.get().load();
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