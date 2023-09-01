package net.forthecrown.sellshop.event;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class ItemSellEvent extends Event {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  private final int sold;
  private final int earned;

  private final Material material;

  private final CommandSyntaxException failure;

  public ItemSellEvent(int sold, int earned, Material material, CommandSyntaxException failure) {
    this.sold = sold;
    this.earned = earned;
    this.material = material;
    this.failure = failure;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }
}
