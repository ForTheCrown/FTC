package net.forthecrown.core.customevents;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.files.Balances;
import net.forthecrown.core.files.FtcUser;
import net.forthecrown.core.files.CrownSignShop;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SignShopUseEvent extends Event implements Cancellable {

    private final FtcUser customer;
    private final FtcUser owner;
    private final Player player;

    private final CrownSignShop shop;
    private final Balances bals;

    private boolean cancelled;

    public SignShopUseEvent(CrownSignShop shop, FtcUser customer, Player player, Balances bals) {
        this.shop = shop;
        this.customer = customer;
        this.player = player;
        this.bals = bals;
        owner = FtcCore.getUser(shop.getOwner());
    }

    public CrownSignShop getShop() {
        return shop;
    }

    public FtcUser getCustomer() {
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
        bals.addBalance(shop.getOwner(), amount, true);
    }

    public void addCustomerBalance(Integer amount){
        bals.addBalance(customer.getBase(), amount, true);
    }

    public FtcUser getOwner(){
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
