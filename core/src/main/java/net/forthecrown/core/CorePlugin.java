package net.forthecrown.core;

import lombok.Getter;
import net.forthecrown.BukkitServices;
import net.forthecrown.Cooldowns;
import net.forthecrown.FtcServer;
import net.forthecrown.InventoryStorage;
import net.forthecrown.WorldEditHook;
import net.forthecrown.command.help.FtcHelpList;
import net.forthecrown.core.commands.CoreCommands;
import net.forthecrown.core.commands.help.HelpListImpl;
import net.forthecrown.core.grave.GraveImpl;
import net.forthecrown.core.listeners.CoreListeners;
import net.forthecrown.core.user.UserServiceImpl;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.user.UserService;
import net.forthecrown.user.Users;
import net.forthecrown.utils.PeriodicalSaver;
import net.forthecrown.utils.TomlConfigs;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class CorePlugin extends JavaPlugin {

  private UserServiceImpl userService;
  private HelpListImpl helpList;
  private FtcServerImpl ftcServer;

  private CoreConfig ftcConfig;

  private PeriodicalSaver saver;

  private DayChange dayChange;

  public static CorePlugin plugin() {
    return getPlugin(CorePlugin.class);
  }

  @Override
  public void onEnable() {
    CoreDataFix.execute();

    Grenadier.plugin(this);
    GraveImpl.init();

    helpList = new HelpListImpl();
    userService = new UserServiceImpl(this);
    ftcServer = new FtcServerImpl();
    dayChange = new DayChange();

    BukkitServices.register(FtcServer.class, ftcServer);
    BukkitServices.register(FtcHelpList.class, helpList);
    BukkitServices.register(InventoryStorage.class, InventoryStorageImpl.getStorage());
    BukkitServices.register(Cooldowns.class, CooldownsImpl.getCooldowns());
    BukkitServices.register(UserService.class, userService);
    BukkitServices.register(WorldEditHook.class, new WorldEditHookImpl());

    Users.setService(userService);
    userService.initialize();
    userService.load();

    CoreListeners.registerAll();
    CoreCommands.createCommands();
    PrefsBook.init(ftcServer.getGlobalSettingsBook());

    saver = PeriodicalSaver.create(this::save, () -> ftcConfig.autosaveInterval);
    reloadConfig();
  }

  @Override
  public void reloadConfig() {
    ftcConfig = TomlConfigs.loadPluginConfig(this, CoreConfig.class);
    saver.start();
    dayChange.schedule();
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

  public void reload() {
    reloadConfig();
    userService.load();
    InventoryStorageImpl.getStorage().load();
    CooldownsImpl.getCooldowns().load();
  }
}