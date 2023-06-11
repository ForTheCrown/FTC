package net.forthecrown.user.event;

import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public class UserAfkEvent extends PlayerEvent {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  private final User user;

  @Setter
  private Component message;

  @Setter
  private Predicate<User> messageViewFilter = user -> true;

  public UserAfkEvent(
      User user,
      Component message
  ) {
    super(user.getPlayer());
    this.user = user;
    this.message = message;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }
}