package net.forthecrown.core;

import java.util.function.Supplier;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.core.admin.BannedWords;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.core.challenge.ChallengeLogs;
import net.forthecrown.core.challenge.ChallengeManager;
import net.forthecrown.core.config.ConfigManager;
import net.forthecrown.core.config.Configs;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.module.ModuleService;
import net.forthecrown.core.module.ModuleServices;
import net.forthecrown.core.module.OnEnable;
import net.forthecrown.core.resource.ResourceWorld;
import net.forthecrown.core.resource.ResourceWorldTracker;
import net.forthecrown.core.script2.ScriptManager;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.datafix.Transformers;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.dungeons.DungeonManager;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.Transactions;
import net.forthecrown.events.Events;
import net.forthecrown.events.MobHealthBar;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.guilds.unlockables.Unlockables;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.inventory.weapon.ability.AbilityAnimation;
import net.forthecrown.inventory.weapon.ability.SwordAbilityManager;
import net.forthecrown.log.LogManager;
import net.forthecrown.structure.Structures;
import net.forthecrown.useables.Usables;
import net.forthecrown.user.Components;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.data.UserRanks;
import net.forthecrown.user.packet.PacketListeners;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.dialogue.DialogueManager;
import net.forthecrown.utils.world.WorldLoader;
import net.forthecrown.waypoint.WaypointManager;
import net.forthecrown.waypoint.WaypointProperties;
import net.forthecrown.waypoint.type.WaypointTypes;
import org.apache.logging.log4j.Logger;


/**
 * A class which loads and creates everything the FTC plugin does and needs. Aka a class which
 * starts the plugin
 */
final class BootStrap {
  private BootStrap() {}

  private static final Logger LOGGER = Loggers.getPluginLogger();

  static void init() {
    long freeMem = Runtime.getRuntime().freeMemory();

    ModuleService onEnable = ModuleService.of(OnEnable.class);
    ModuleServices.SERVICES.register("on_enable", onEnable);

    // Tools
    init(InventoryStorage::getStorage);
    init(Cooldowns::getCooldowns);
    init(DialogueManager::getDialogues);

    // Guilds
    init(Unlockables.class);
    init(GuildManager::get);

    // Users
    init(UserManager::get);
    init(UserRanks.class);
    init(Components.class);
    init(Properties.class);

    // Transformers
    // Transformers.runCurrent() actually calls all the data updaters,
    // and it should always be the last method call in this method
    init(Transformers.class);

    // Waypoints
    init(WaypointTypes.class);
    init(WaypointProperties.class);
    init(WaypointManager::getInstance);

    // Structures
    // Should be loaded before dungeons, as dungeons
    // might potentially need to access the structures
    init(Structures::get);

    // Dungeons
    init(Bosses.class);

    // Only load dungeons for testing purposes
    if (FTC.inDebugMode()) {
      init(DungeonManager::getDungeons);
    }

    // Item stuff, like crown and royal sword
    init(SwordAbilityManager::getInstance);
    init(ExtendedItems.class);

    // Bunch of miscellaneous modules
    init(AbilityAnimation::getInstance);
    init(Cosmetics.class);
    init(Usables::getInstance);
    init(ResourceWorldTracker::get);
    init(Announcer::get);
    init(Economy::get);
    init(Configs.class);
    init(ConfigManager::get);
    init(MobHealthBar.class);
    init(WorldLoader.class);
    init(PacketListeners.class);
    init(Punishments::get);

    // The following 2 classes must be loaded
    // before the data manager, they register
    // the data log schemas required.
    init(ChallengeLogs.class);
    init(Transactions.class);
    init(LogManager::getInstance);

    // Must be initialized after the data manager
    // since it queries it to find the currently
    // active challenges
    init(ChallengeManager::getInstance);

    // Only day change listeners
    init(ResourceWorld::get);
    init(EndOpener::get);
    init(Economy.get()::getMarkets);

    // Commands and events
    init(Commands.class);
    init(Events.class);

    init(ScriptManager::getInstance);
    init(ServerListDisplay::getInstance);

    // Save and load the banner words list
    FTC.getPlugin().saveResource("banned_words.json", true);
    BannedWords.load();

    // Schedule and run module services
    ModuleServices.DAY_CHANGE.schedule();
    ModuleServices.SAVE.schedule();

    onEnable.run();
    ModuleServices.RELOAD.run();

    onEnable.getCallbacks().clear();
    ModuleServices.SERVICES.remove("on_enable");

    Transformers.runCurrent();

    long freeMemAfter = Runtime.getRuntime().freeMemory();
    long lostMemory = freeMemAfter - freeMem;
    LOGGER.debug("Plugin initialization took {}MB or {} bytes",
        lostMemory / 1024 / 1024,
        lostMemory
    );
  }

  static <T> void init(Supplier<T> supplier) {
    init(supplier.get());
  }

  // There is no way Object#getClass() returns anything other than
  // its own class, ie, T#getClass() MUST return a Class<T>
  @SuppressWarnings("unchecked")
  static <T> void init(T instance) {
    _init(instance, (Class<T>) instance.getClass());
  }

  static void init(Class<?> clazz) {
    _init(null, clazz);
  }

  static <T> void _init(T instance, Class<T> c) {
    try {
      for (var s : ModuleServices.SERVICES) {
        s.addAll(c, instance);
      }
    } catch (Throwable t) {
      LOGGER.error("Couldn't initialize {}:", c.getSimpleName(), t);
    }
  }
}