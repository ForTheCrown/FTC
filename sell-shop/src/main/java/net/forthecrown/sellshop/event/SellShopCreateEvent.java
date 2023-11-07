package net.forthecrown.sellshop.event;

import lombok.Getter;
import net.forthecrown.menu.MenuBuilder;
import net.forthecrown.sellshop.SellShop;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class SellShopCreateEvent extends Event {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  private final MenuBuilder builder;
  private final SellShop shop;

  public SellShopCreateEvent(MenuBuilder builder, SellShop shop) {
    this.builder = builder;
    this.shop = shop;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }
}
