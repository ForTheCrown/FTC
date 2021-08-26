package net.forthecrown.events;

import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.PluginManager;

public final class Events {
    private Events() {}

    private static PluginManager pm;
    private static Crown main;

    public static void init(){
        main = Crown.inst();
        pm = main.getServer().getPluginManager();

        register(new JeromeListener());

        register(new CoreListener());
        register(new GraveListener());

        register(new ShopCreateListener());
        register(new ShopInteractionListener());
        register(new ShopDestroyListener());

        register(new RegionsListener());

        register(new PirateEvents());
        register(new GhListener());

        register(new MobHealthBar());
        register(new SmokeBomb());
        register(new VolleyBallListener());

        register(new UsablesListeners());

        register(new MarriageListener());

        register(new CosmeticsListener());
        register(new CustomInventoryClickListener());
        register(new InventoryBuilderListener());

        register(new DungeonListeners());
        register(new EnchantListeners());

        main = null;
        pm = null;
    }

    private static void register(Listener listener){
        pm.registerEvents(listener, main);
    }

    public static <E extends PlayerEvent> void handlePlayer(E event, ExceptionedListener<E> executor){
        handle(event.getPlayer(), event, executor);
    }

    public static <E extends Event> void handle(CommandSender sender, E event, ExceptionedListener<E> executor){
        try {
            executor.execute(event);
        } catch (RoyalCommandException e){
            if(sender == null) return;

            sender.sendMessage(e.getComponentMessage().colorIfAbsent(NamedTextColor.GRAY));
        } catch (RuntimeException e){
            e.printStackTrace();
        }
    }
}
