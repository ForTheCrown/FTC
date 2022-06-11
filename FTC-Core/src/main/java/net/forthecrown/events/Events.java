package net.forthecrown.events;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcDiscord;
import net.forthecrown.events.dynamic.JailCellListener;
import net.forthecrown.user.packet.Packets;
import net.forthecrown.utils.FtcUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
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

    public static void init() {
        register(new CoreListener());

        register(new ShopCreateListener());
        register(new ShopInteractionListener());
        register(new ShopDestroyListener());

        register(new RegionsListener());
        register(new MarketListener());

        register(new MobHealthBar());
        register(new SmokeBomb());

        register(new UsablesListeners());

        register(new MarriageListener());

        register(new CosmeticsListener());
        register(new InventoryBuilderListener());

        register(new DungeonListeners());
        register(new EnchantListeners());

        register(new JailCellListener());

        register(new ResourceWorldListener());
        register(new WeaponListener());

        if (FtcDiscord.isActive()) {
            Crown.logger().info("Discord listener registered");
            FtcDiscord.getHandle().getJda().addEventListener(
                    new DiscordListener()
            );
        }

        Packets.register(new PlayerPacketListener());

        Crown.logger().info("Events registered");
    }

    /**
     * Registers the given listener
     * @param listener The listener to register
     */
    public static void register(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, Crown.inst());
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
    public static <E extends Event> void handle(@Nullable CommandSender sender, E event, ExceptionedListener<E> executor) {
        try {
            executor.execute(event);
        } catch (CommandSyntaxException e) {
            if(sender == null) return;

            FtcUtils.handleSyntaxException(sender, e);
        } catch (RuntimeException e) {
            Crown.logger().error("Error running listener for event " + event.getClass().getSimpleName(), e);
        }
    }
}