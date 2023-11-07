package net.forthecrown.core;

import lombok.Getter;
import net.forthecrown.BukkitServices;
import net.forthecrown.Cooldowns;
import net.forthecrown.FtcServer;
import net.forthecrown.InventoryStorage;
import net.forthecrown.WorldEditHook;
import net.forthecrown.command.help.FtcHelpList;
import net.forthecrown.core.announcer.AutoAnnouncer;
import net.forthecrown.core.commands.CoreCommands;
import net.forthecrown.core.commands.help.HelpListImpl;
import net.forthecrown.core.grave.GraveImpl;
import net.forthecrown.core.listeners.CoreListeners;
import net.forthecrown.core.listeners.MobHealthBar;
import net.forthecrown.core.placeholder.PlaceholderServiceImpl;
import net.forthecrown.core.user.UserServiceImpl;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.text.placeholder.PlaceholderService;
import net.forthecrown.user.UserService;
import net.forthecrown.user.Users;
import net.forthecrown.utils.PeriodicalSaver;
import net.forthecrown.utils.TomlConfigs;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class CorePlugin extends JavaPlugin {

  private CoreConfig ftcConfig;
  private PeriodicalSaver saver;

  private UserServiceImpl userService;
  private HelpListImpl helpList;
  private FtcServerImpl ftcServer;
  private PlaceholderServiceImpl placeholderService;
  private DayChange dayChange;
  private AutoAnnouncer announcer;
  private JoinInfo joinInfo;
  private Wild wild;
  private EmojiLoader emojiLoader;

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
    ftcServer = new FtcServerImpl(this);
    dayChange = new DayChange();
    announcer = new AutoAnnouncer();
    placeholderService = new PlaceholderServiceImpl(this);
    joinInfo = new JoinInfo();
    wild = new Wild();
    emojiLoader = new EmojiLoader();

    BukkitServices.register(FtcServer.class, ftcServer);
    BukkitServices.register(FtcHelpList.class, helpList);
    BukkitServices.register(InventoryStorage.class, InventoryStorageImpl.getStorage());
    BukkitServices.register(Cooldowns.class, CooldownsImpl.getCooldowns());
    BukkitServices.register(UserService.class, userService);
    BukkitServices.register(WorldEditHook.class, new WorldEditHookImpl());
    BukkitServices.register(PlaceholderService.class, placeholderService);

    Users.setService(userService);
    userService.initialize();

    CoreListeners.registerAll(this);
    CoreCommands.createCommands(this);
    PrefsBook.init(ftcServer.getGlobalSettingsBook());

    saver = PeriodicalSaver.create(this::save, () -> ftcConfig.autosaveInterval());

    reloadAll();
  }

  @Override
  public void onLoad() {
    CoreFlags.registerAll();
  }

  @Override
  public void onDisable() {
    save();
    MobHealthBar.shutdown();
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
    ftcServer.save();

    InventoryStorageImpl.getStorage().save();
    CooldownsImpl.getCooldowns().save();
  }

  public void reloadAll() {
    userService.load();
    InventoryStorageImpl.getStorage().load();
    CooldownsImpl.getCooldowns().load();

    reload();
  }

  public void reload() {
    reloadConfig();

    announcer.load();
    announcer.start();

    joinInfo.load();
    placeholderService.load();
    ftcServer.load();
    helpList.load();
    wild.load();
    emojiLoader.load();
  }
}