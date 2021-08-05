package net.forthecrown.events;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.PluginManager;

public class Events {
    private static PluginManager pm;
    private static ForTheCrown main;

    public static void init(){
        main = ForTheCrown.inst();
        pm = main.getServer().getPluginManager();

        register(new JeromeEvent());

        register(new CoreListener());
        register(new GraveListener());

        register(new ShopCreateEvent());
        register(new SignInteractEvent());
        register(new ShopDestroyEvent());

        register(new PirateEvents());
        register(new GhListener());

        register(new MobHealthBar());
        register(new SmokeBomb());
        register(new VolleyBallListener());

        register(new InteractableEvents());

        register(new MarriageListener());

        register(new CosmeticsListener());
        register(new CustomInventoryClickListener());
        register(new InventoryBuilderListener());

        register(new DungeonEvents());
        register(new EnchantEvents());

        main = null;
        pm = null;
    }

    private static void register(Listener listener){
        pm.registerEvents(listener, main);
    }

    public static <E extends PlayerEvent> void handlePlayer(E event, ExceptionedEvent<E> executor){
        handle(event.getPlayer(), event, executor);
    }

    public static <E extends Event> void handle(CommandSender sender, E event, ExceptionedEvent<E> executor){
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
