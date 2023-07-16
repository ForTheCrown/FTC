package net.forthecrown.user.event;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.Permissions;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

@Getter
@Setter
public abstract class UserLogEvent extends UserEvent {

  private boolean showMessage;

  private UserLogRenderer renderer;

  /**
   * Refers to the event being called only for the message render, the user has not actually left
   * or joined the server
   */
  private final boolean messageOnly;

  public UserLogEvent(User user, UserLogRenderer renderer, boolean messageOnly) {
    super(user);

    this.showMessage = true;
    this.renderer = renderer;
    this.messageOnly = messageOnly;
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

      if (message == null) {
        return;
      }

      if (source.get(Properties.VANISHED)) {
        if (!player.hasPermission(Permissions.VANISH_SEE)) {
          return;
        }

        message = Component.textOfChildren(
            Component.text("[Only visible to staff] ", NamedTextColor.DARK_GRAY),
            message
        );
      }

      player.sendMessage(message);
    });
  }
}