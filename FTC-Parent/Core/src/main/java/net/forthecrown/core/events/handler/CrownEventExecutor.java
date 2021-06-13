package net.forthecrown.core.events.handler;

import net.forthecrown.core.CrownException;
import net.forthecrown.core.events.custom.SellShopUseEvent;
import net.forthecrown.core.events.custom.SignShopUseEvent;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;

public class CrownEventExecutor {

    public static <T extends PlayerEvent> void handlePlayer(T event, ExceptionedEvent<T> executor){
        handle(event.getPlayer(), event, executor);
    }

    public static void handleSignShop(SignShopUseEvent event, ExceptionedEvent<SignShopUseEvent> executor){
        handle(event.getUser(), event, executor);
    }

    public static void handleSellShop(SellShopUseEvent event, ExceptionedEvent<SellShopUseEvent> executor){
        handle(event.getUser(), event, executor);
    }

    private static <T extends Event> void handle(CommandSender sender, T event, ExceptionedEvent<T> executor){
        try {
            executor.onEvent(event);
        } catch (CrownException ignored){
        } catch (RoyalCommandException e){
            if(sender == null) return;

            sender.sendMessage(e.formattedText());
        } catch (RuntimeException e){
            e.printStackTrace();
        }
    }
}
