package net.forthecrown.user.event;

import net.forthecrown.text.Messages;
import net.forthecrown.user.NameRenderFlags;
import net.forthecrown.user.User;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

public interface UserLogRenderer {

  UserLogRenderer DEFAULT_JOIN = (user, viewer) -> {
    return Messages.joinMessage(user.displayName(viewer));
  };

  UserLogRenderer DEFAULT_LEAVE = (user, viewer) -> {
    // Calling this method overrides the automatic addition of the USER_ONLINE flag,
    // since during the quit event, the player is still online, but stuff like click
    // events in the display name require the player to be marked as offline
    return Messages.leaveMessage(user.displayName(viewer, NameRenderFlags.ALLOW_NICKNAME));
  };

  Component render(User user, Audience viewer);
}