package net.forthecrown.user.event;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.user.User;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerQuitEvent.QuitReason;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
public class UserLeaveEvent extends UserLogEvent {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  private final QuitReason reason;

  public UserLeaveEvent(User user, QuitReason reason) {
    super(user, UserLogRenderer.defaultLeave(reason));
    this.reason = reason;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }
}