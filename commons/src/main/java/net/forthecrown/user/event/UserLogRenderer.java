package net.forthecrown.user.event;

import net.forthecrown.Permissions;
import net.forthecrown.text.Messages;
import net.forthecrown.user.NameRenderFlags;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.forthecrown.utils.Audiences;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.PlayerQuitEvent.QuitReason;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UserLogRenderer {

  UserLogRenderer DEFAULT_JOIN = (user, viewer) -> {
    return Messages.joinMessage(user.displayName(viewer));
  };

  static UserLogRenderer defaultLeave(QuitReason reason) {
    return (user, viewer) -> {
      // Calling this method overrides the automatic addition of the USER_ONLINE flag,
      // since during the quit event, the player is still online, but stuff like click
      // events in the display name require the player to be marked as offline
      return Messages.leaveMessage(
          user.displayName(viewer, NameRenderFlags.ALLOW_NICKNAME),
          reason
      );
    };
  }

  /**
   * Renders the login/logout message
   * @param user User that joined/left
   * @param viewer Viewer seeing the message
   * @return Rendered message, or {@code null}, to not show a message to the viewer
   */
  @Nullable
  Component render(@NotNull User user, @NotNull Audience viewer);
}