package net.forthecrown.emperor.events.custom;

import net.forthecrown.emperor.economy.Balances;
import net.forthecrown.emperor.economy.shops.SignShop;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player uses a signshop
 */
public class SignShopUseEvent extends Event implements Cancellable {

    private final CrownUser customer;
    private final CrownUser owner;
    private final Player player;

    private final SignShop shop;
    private final Balances bals;

    private boolean cancelled;

    public SignShopUseEvent(SignShop shop, CrownUser customer, Player player, Balances bals) {
        this.shop = shop;
        this.customer = customer;
        this.player = player;
        this.bals = bals;
        owner = UserManager.getUser(shop.getOwner());
    }

    public SignShop getShop() {
        return shop;
    }

    public CrownUser getCustomer() {
        return customer;
    }

    public Player getPlayer() {
        return player;
    }

    public Balances getBalances() {
        return bals;
    }

    public Integer getCustomerBalance(){
        return bals.get(customer.getUniqueId());
    }

    public void setCustomerBalance(Integer amount){
        bals.set(customer.getUniqueId(), amount);
    }

    public Integer getOwnerBalance(){
        return bals.get(shop.getOwner());
    }

    public void setOwnerBalance(Integer amount){
        bals.set(shop.getOwner(), amount);
    }

    public void addOwnerBalance(Integer amount){
        if(!getCustomer().getUniqueId().equals(getOwner().getPlayer())) bals.add(getOwner().getUniqueId(), amount, true);
        else bals.add(shop.getOwner(), amount);
    }

    public void addCustomerBalance(Integer amount){
        if(!getCustomer().getUniqueId().equals(getOwner().getPlayer())) bals.add(getCustomer().getUniqueId(), amount, true);
        else bals.add(getCustomer().getUniqueId(), amount);
    }

    public CrownUser getOwner(){
        return owner;
    }


    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
