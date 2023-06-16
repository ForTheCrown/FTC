package net.forthecrown.user.event;

import java.util.Objects;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class UserAfkEvent extends UserEvent {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  @Setter
  private Component message;

  private Predicate<User> messageViewFilter = user -> true;

  public UserAfkEvent(User user, Component message) {
    super(user);
    this.message = message;
  }

  public void setMessageViewFilter(Predicate<User> messageViewFilter) {
    Objects.requireNonNull(messageViewFilter);
    this.messageViewFilter = messageViewFilter;
  }

  public void addFilter(Predicate<User> viewFilter) {
    Objects.requireNonNull(viewFilter);
    this.messageViewFilter = messageViewFilter.and(viewFilter);
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }
}