package net.forthecrown.events.custom;

import net.forthecrown.economy.shops.ShopCustomer;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.economy.shops.SignShopSession;
import net.forthecrown.user.UserManager;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SignShopUseEvent extends Event {
    private final ShopCustomer customer;
    private final SignShopSession session;
    private final SignShop shop;

    public SignShopUseEvent(SignShopSession session) {
        this.customer = session.getCustomer();
        this.session = session;
        this.shop = session.getShop();
    }

    public boolean customerIsPlayer() {
        return UserManager.isPlayerID(customer.getUniqueId());
    }

    public ShopCustomer getCustomer() {
        return customer;
    }

    public SignShop getShop() {
        return shop;
    }

    public SignShopSession getSession() {
        return session;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
