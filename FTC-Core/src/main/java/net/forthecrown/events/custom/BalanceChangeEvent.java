package net.forthecrown.events.custom;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BalanceChangeEvent extends Event {
    private final UUID balanceHolder;
    private final Action action;
    private final int currentBalance;
    private final int changeAmount;

    public BalanceChangeEvent(UUID balanceHolder, Action action, int currentBalance, int changeAmount) {
        this.balanceHolder = balanceHolder;
        this.action = action;
        this.currentBalance = currentBalance;
        this.changeAmount = changeAmount;
    }

    public Action getAction() {
        return action;
    }

    public int getChangeAmount() {
        return changeAmount;
    }

    public int getCurrentBalance() {
        return currentBalance;
    }

    public UUID getBalanceHolder() {
        return balanceHolder;
    }

    public enum Action {
        ADD,
        REMOVE
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
