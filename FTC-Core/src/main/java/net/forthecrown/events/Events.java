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

    public static void register(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, Crown.inst());
    }

    public static void unregister(Listener listener) {
        HandlerList.unregisterAll(listener);
    }

    public static <E extends Event> void handle(CommandSender sender, E event, ExceptionedListener<E> executor) {
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