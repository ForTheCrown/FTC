package net.forthecrown.user.event;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.user.User;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class HomeCommandEvent extends UserEvent implements Cancellable {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  private final String homeName;

  private final boolean nameSet;

  @Setter
  private boolean cancelled;

  public HomeCommandEvent(User user, boolean nameSet, String homeName) {
    super(user);
    this.nameSet = nameSet;
    this.homeName = homeName;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }
}
