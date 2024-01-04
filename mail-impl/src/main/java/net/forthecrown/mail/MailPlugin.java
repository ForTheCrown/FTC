package net.forthecrown.mail;

import java.time.Duration;
import lombok.Getter;
import net.forthecrown.BukkitServices;
import net.forthecrown.FtcServer;
import net.forthecrown.mail.command.MailCommands;
import net.forthecrown.mail.listeners.MailListeners;
import net.forthecrown.utils.PeriodicalSaver;
import net.forthecrown.utils.TomlConfigs;
import org.bukkit.plugin.java.JavaPlugin;

public class MailPlugin extends JavaPlugin {

  @Getter
  private ServiceImpl service;
  private PeriodicalSaver saver;

  @Getter
  private MailConfig mailConfig;

  @Override
  public void onEnable() {
    service = new ServiceImpl(this);
    BukkitServices.register(MailService.class, service);

    reloadConfig();

    MailCommands.createCommands(service);
    MailListeners.registerAll(service);

    service.load();

    saver = PeriodicalSaver.create(service::save, () -> Duration.ofMinutes(30));
    saver.start();

    var server = FtcServer.server();
    MailPrefs.init(server.getGlobalSettingsBook());
  }

  @Override
  public void onDisable() {
    service.save();
  }

  @Override
  public void reloadConfig() {
    mailConfig = TomlConfigs.loadPluginConfig(this, MailConfig.class);
  }
}
