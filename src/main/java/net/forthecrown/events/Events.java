package net.forthecrown.events;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.FTC;
import net.forthecrown.core.module.OnEnable;
import net.forthecrown.events.economy.*;
import net.forthecrown.events.player.*;
import net.forthecrown.user.packet.PacketListeners;
import net.forthecrown.utils.Util;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

/**
 * A class for some general utility methods
 * relating to event listeners
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
        register(new TrapDoorListener());
        register(new PlaytimeChallengeListener());

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

        register(new AutoSellListener());
        register(new DurabilityListener());
        register(new PlayerMoveGuildChunkListener());
        register(new PotionEffectListener());

        // Listen for voting plugin votes
        if (Util.isPluginEnabled("VotingPlugin")) {
            register(new VoteListener());
        }

        PacketListeners.register(new PlayerPacketListener());
        PacketListeners.register(new ChatPacketListener());
    }

    /**
     * Registers the given listener
     * @param listener The listener to register
     */
    public static void register(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, FTC.getPlugin());
    }

    /**
     * Unregisters the given listener
     * @param listener The listener to unregister
     */
    public static void unregister(Listener listener) {
        HandlerList.unregisterAll(listener);
    }

    /**
     * Handles an event which may throw a {@link CommandSyntaxException}
     * @param sender The sender involved in the event, may be null
     * @param event The event to handle
     * @param executor The executor which may throw an exception
     * @param <E> The event type
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
            FTC.getLogger().error(
                    "Error executing event {}",
                    event.getEventName(),
                    e
            );
        }
    }
}