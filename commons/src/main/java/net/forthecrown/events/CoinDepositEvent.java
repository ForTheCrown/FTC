package net.forthecrown.events;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemList;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
public class CoinDepositEvent extends Event implements Cancellable {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  private final User depositor;
  private final ItemList coinItems;

  private int depositedCoins;
  private int earned;

  private boolean cancelled;

  public CoinDepositEvent(
      User depositor,
      ItemList coinItems,
      int depositedCoins,
      int earned
  ) {
    this.depositor = depositor;
    this.coinItems = coinItems;
    this.depositedCoins = depositedCoins;
    this.earned = earned;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }
}
