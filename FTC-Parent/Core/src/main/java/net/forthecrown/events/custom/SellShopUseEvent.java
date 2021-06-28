package net.forthecrown.events.custom;

import net.forthecrown.economy.Balances;
import net.forthecrown.economy.SellShop;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.enums.SellAmount;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Called when a player uses the server's sellshop
 */
public class SellShopUseEvent extends Event implements Cancellable {

    private final CrownUser seller;
    private final Balances balances;
    private final Player sellerPlayer;

    private final ItemStack clickedItem;
    private final Material item;
    private final SellAmount sellAmount;
    private final SellShop menu;

    private boolean cancelled;

    public SellShopUseEvent(CrownUser seller, Balances balances, Material item, ItemStack clickedItem, SellShop shop) {
        this.seller = seller;
        this.menu = shop;
        this.balances = balances;
        this.sellerPlayer = seller.getPlayer();
        this.clickedItem = clickedItem;
        this.item = item;
        this.sellAmount = seller.getSellAmount();
    }

    public CrownUser getUser() {
        return seller;
    }

    public Balances getBalances() {
        return balances;
    }

    public Player getSellerPlayer() {
        return sellerPlayer;
    }

    public Material getItem() {
        return item;
    }

    public SellAmount getSellAmount() {
        return sellAmount;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public SellShop getShop() {
        return menu;
    }

    public ItemStack getClickedItem() {
        return clickedItem;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
