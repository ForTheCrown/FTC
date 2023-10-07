package net.forthecrown.sellshop.event;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.user.User;
import net.forthecrown.user.event.UserEvent;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class ItemSellEvent extends UserEvent {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  private final int sold;
  private final int earned;

  private final Material material;

  private final CommandSyntaxException failure;

  public ItemSellEvent(
      User user,
      int sold,
      int earned,
      Material material,
      CommandSyntaxException failure
  ) {
    super(user);
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
