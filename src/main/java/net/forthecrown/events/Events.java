package net.forthecrown.events;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.scarsz.discordsrv.DiscordSRV;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.FTC;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.module.OnEnable;
import net.forthecrown.events.economy.AutoSellListener;
import net.forthecrown.events.economy.MarketListener;
import net.forthecrown.events.economy.ShopCreateListener;
import net.forthecrown.events.economy.ShopDestroyListener;
import net.forthecrown.events.economy.ShopInteractionListener;
import net.forthecrown.events.economy.ShopInventoryListener;
import net.forthecrown.events.guilds.GuildDiscordListener;
import net.forthecrown.events.guilds.GuildEvents;
import net.forthecrown.events.player.AfkListener;
import net.forthecrown.events.player.ChatListener;
import net.forthecrown.events.player.ChatPacketListener;
import net.forthecrown.events.player.DurabilityListener;
import net.forthecrown.events.player.JailListener;
import net.forthecrown.events.player.LoginListener;
import net.forthecrown.events.player.MarriageListener;
import net.forthecrown.events.player.MotdListener;
import net.forthecrown.events.player.PlayerDeathListener;
import net.forthecrown.events.player.PlayerDiscordBoostListener;
import net.forthecrown.events.player.PlayerJoinListener;
import net.forthecrown.events.player.PlayerLeaveListener;
import net.forthecrown.events.player.PlayerPacketListener;
import net.forthecrown.events.player.PlayerTeleportListener;
import net.forthecrown.events.player.WeaponListener;
import net.forthecrown.user.packet.PacketListeners;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Util;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

/**
 * A class for some general utility methods relating to event listeners
 */
public final class Events {
  private Events() {}

  /**
   * Initializes all FTC listeners
   */
  @OnEnable
  private static void init() {
    register(new CoreListener());
    register(new ChatListener());
    register(new AfkListener());
    register(new EavesDropListener());
    register(new MotdListener());
    register(new PlayerTeleportListener());
    register(new PlayerDeathListener());
    register(new TrapDoorListener());

    // Join / Quit listeners
    register(new PlayerJoinListener());
    register(new PlayerLeaveListener());

    // Shop listeners
    register(new ShopCreateListener());
    register(new ShopInteractionListener());
    register(new ShopDestroyListener());
    register(new ShopInventoryListener());

    register(new WaypointListener());
    register(new WaypointDestroyListener());
    register(new MarketListener());

    // Random features
    register(new MobHealthBar());
    register(new SmokeBomb());

    register(new UsablesListeners());
    register(new CosmeticsListener());
    register(new JailListener());
    register(new MarriageListener());
    register(new InventoryMenuListener());
    register(new NoCopiesListener());
    register(new AnvilListener());

    // Dungeons Listeners
    register(new DungeonListeners());
    register(new EnchantListeners());
    register(new PunchingBags());

    register(new ResourceWorldListener());
    register(new WeaponListener());
    register(new SwordFireballListener());

    register(new AutoSellListener());
    register(new DurabilityListener());

    register(new CommandBroadcastListener());
    register(new SignOwnershipListener());

    GuildEvents.registerAll();

    register(new LoginListener());

    // Listen for voting plugin votes
    if (Util.isPluginEnabled("VotingPlugin")) {
      register(new VoteListener());
    }

    PacketListeners.register(new PlayerPacketListener());
    PacketListeners.register(new ChatPacketListener());

    var api = DiscordSRV.api;
    api.subscribe(new GuildDiscordListener());
    api.subscribe(new AnnouncementForwardingListener());
    api.subscribe(new DiscordStaffChatListener());

    Tasks.runLater(() -> {
      var jda = DiscordSRV.getPlugin().getJda();

      if (jda == null) {
        Loggers.getLogger()
            .warn("No JDA found, cannot register boost listeners");

        return;
      }

      jda.addEventListener(new PlayerDiscordBoostListener());
      Loggers.getLogger().info("Registered boost listener");
    }, 40);
  }

  /**
   * Registers the given listener
   *
   * @param listener The listener to register
   */
  public static void register(Listener listener) {
    Bukkit.getPluginManager().registerEvents(listener, FTC.getPlugin());
  }

  /**
   * Unregisters the given listener
   *
   * @param listener The listener to unregister
   */
  public static void unregister(Listener listener) {
    HandlerList.unregisterAll(listener);
  }

  /**
   * Handles an event which may throw a {@link CommandSyntaxException}
   *
   * @param sender   The sender involved in the event, may be null
   * @param event    The event to handle
   * @param executor The executor which may throw an exception
   * @param <E>      The event type
   */
  public static <E extends Event> void runSafe(@Nullable Audience sender,
                                               E event,
                                               ThrowingListener<E> executor
  ) {
    try {
      executor.execute(event);
    } catch (CommandSyntaxException e) {
      if (sender == null) {
        return;
      }

      Exceptions.handleSyntaxException(sender, e);
    } catch (Throwable e) {
      Loggers.getLogger().error(
          "Error executing event {}",
          event.getEventName(),
          e
      );
    }
  }
}