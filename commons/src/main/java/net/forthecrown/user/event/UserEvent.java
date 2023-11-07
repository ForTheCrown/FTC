package net.forthecrown.user.event;

import lombok.Getter;
import net.forthecrown.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

@Getter
public abstract class UserEvent extends Event {

  private final User user;

  public UserEvent(User user) {
    this.user = user;
  }

  public UserEvent(User user, boolean isAsync) {
    super(isAsync);
    this.user = user;
  }

  public Player getPlayer() {
    return user.getPlayer();
  }
}