package net.forthecrown.core.customevents;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.SignShop;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

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
        owner = FtcCore.getUser(shop.getOwner());
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
        return bals.getBalance(customer.getBase());
    }

    public void setCustomerBalance(Integer amount){
        bals.setBalance(customer.getBase(), amount);
    }

    public Integer getOwnerBalance(){
        return bals.getBalance(shop.getOwner());
    }

    public void setOwnerBalance(Integer amount){
        bals.setBalance(shop.getOwner(), amount);
    }

    public void addOwnerBalance(Integer amount){
        if(!getCustomer().getBase().equals(getOwner().getBase())) bals.addBalance(getOwner().getBase(), amount, true);
        else bals.addBalance(shop.getOwner(), amount);
    }

    public void addCustomerBalance(Integer amount){
        if(!getCustomer().getBase().equals(getOwner().getBase())) bals.addBalance(getCustomer().getBase(), amount, true);
        else bals.addBalance(getCustomer().getBase(), amount);
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
