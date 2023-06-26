package net.forthecrown.user.event;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.user.User;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
public class UserJoinEvent extends UserLogEvent {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  private final String lastOnlineName;

  private final boolean firstJoin;

  public UserJoinEvent(User user, String lastOnlineName, boolean firstJoin) {
    super(user, UserLogRenderer.DEFAULT_JOIN);
    this.lastOnlineName = lastOnlineName;
    this.firstJoin = firstJoin;
  }

  public boolean hasNameChanged() {
    return !getUser().getName().equals(lastOnlineName);
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }

}