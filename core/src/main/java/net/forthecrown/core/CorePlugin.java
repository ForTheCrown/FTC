package net.forthecrown.core;

import lombok.Getter;
import net.forthecrown.BukkitServices;
import net.forthecrown.Cooldowns;
import net.forthecrown.FtcServer;
import net.forthecrown.InventoryStorage;
import net.forthecrown.command.help.FtcHelpList;
import net.forthecrown.core.commands.CoreCommands;
import net.forthecrown.core.grave.GraveImpl;
import net.forthecrown.core.help.HelpListImpl;
import net.forthecrown.core.listeners.CoreListeners;
import net.forthecrown.core.user.UserServiceImpl;
import net.forthecrown.user.UserService;
import net.forthecrown.user.Users;
import net.forthecrown.utils.PeriodicalSaver;
import net.forthecrown.utils.TomlConfigs;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class CorePlugin extends JavaPlugin {

  private UserServiceImpl userService;
  private HelpListImpl helpList;
  private FtcServerImpl server;

  private CoreConfig config;

  private PeriodicalSaver saver;

  @Override
  public void onEnable() {
    GraveImpl.init();

    helpList = new HelpListImpl();
    userService = new UserServiceImpl();
    server = new FtcServerImpl();

    BukkitServices.register(FtcServer.class, server);
    BukkitServices.register(FtcHelpList.class, helpList);
    BukkitServices.register(InventoryStorage.class, InventoryStorageImpl.getStorage());
    BukkitServices.register(Cooldowns.class, CooldownsImpl.getCooldowns());
    BukkitServices.register(UserService.class, userService);

    Users.setService(userService);
    userService.initialize();
    userService.load();

    CoreListeners.registerAll();
    CoreCommands.createCommands();

    saver = PeriodicalSaver.create(this::save, () -> config.autosaveInterval);
    reloadConfig();
  }

  @Override
  public void onDisable() {
    userService.shutdown();
  }

  @Override
  public void reloadConfig() {
    config = TomlConfigs.loadPluginConfig(this, CoreConfig.class);
    saver.start();
  }

  @Override
  public void saveConfig() {

  }

  @Override
  public void saveDefaultConfig() {
    saveResource("config.toml", false);
  }

  public void save() {
    userService.save();

    InventoryStorageImpl.getStorage().save();
    CooldownsImpl.getCooldowns().save();
  }
}