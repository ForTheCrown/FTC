package net.forthecrown.core.customevents;

import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.enums.SellAmount;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SellShopSellEvent extends Event implements Cancellable {

    private final CrownUser seller;
    private final Balances balances;
    private final Player sellerPlayer;

    private final Material item;
    private final SellAmount sellAmount;

    private boolean cancelled;

    public SellShopSellEvent(CrownUser seller, Balances balances, Material item) {
        this.seller = seller;
        this.balances = balances;
        this.sellerPlayer = seller.getPlayer();
        this.item = item;
        this.sellAmount = seller.getSellAmount();
    }

    public CrownUser getSeller() {
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
