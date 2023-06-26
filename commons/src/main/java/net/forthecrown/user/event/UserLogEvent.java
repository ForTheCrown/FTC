package net.forthecrown.user.event;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

@Getter
@Setter
public abstract class UserLogEvent extends UserEvent {

  private boolean showMessage;

  private UserLogRenderer renderer;

  public UserLogEvent(User user, UserLogRenderer renderer) {
    super(user);

    this.showMessage = true;
    this.renderer = renderer;
  }

  public void setRenderer(UserLogRenderer renderer) {
    Objects.requireNonNull(renderer);
    this.renderer = renderer;
  }

  public static void maybeAnnounce(UserLogEvent event) {
    if (!event.isShowMessage()) {
      return;
    }

    UserLogRenderer renderer = event.getRenderer();
    User source = event.getUser();

    Bukkit.getOnlinePlayers().forEach(player -> {
      Component message = renderer.render(source, player);
      player.sendMessage(message);
    });
  }
}