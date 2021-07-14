package net.forthecrown.events;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.CrownException;
import net.forthecrown.events.custom.SignShopUseEvent;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.PluginManager;

public class Events {
    private static final PluginManager pm = Bukkit.getPluginManager();
    private static final CrownCore main = CrownCore.inst();

    public static void init(){
        register(new JeromeEvent());
        register(new JackEvent());

        register(new CoreListener());
        register(new GraveListener());

        register(new ShopCreateEvent());
        register(new SignInteractEvent());
        register(new ShopDestroyEvent());
        register(new ShopTransactionEvent());

        register(new PirateEvents());

        register(new MobHealthBar());
        register(new SmokeBomb());
        register(new VolleyBallListener());

        register(new InteractableEvents());

        register(new MarriageListener());

        register(new DungeonEvents());
        register(new EnchantEvents());

        register(new CosmeticsListener());
        register(new CustomInventoryClickListener());
        register(new InventoryBuilderListener());
    }

    private static void register(Listener listener){
        pm.registerEvents(listener, main);
    }

    public static <E extends PlayerEvent> void handlePlayer(E event, ExceptionedEvent<E> executor){
        handle(event.getPlayer(), event, executor);
    }

    public static void handleSignShop(SignShopUseEvent event, ExceptionedEvent<SignShopUseEvent> executor){
        handle(event.getUser(), event, executor);
    }

    public static <E extends Event> void handle(CommandSender sender, E event, ExceptionedEvent<E> executor){
        try {
            executor.execute(event);
        } catch (CrownException ignored){
        } catch (RoyalCommandException e){
            if(sender == null) return;

            sender.sendMessage(e.getComponentMessage().colorIfAbsent(NamedTextColor.GRAY));
        } catch (RuntimeException e){
            e.printStackTrace();
        }
    }
}
