package net.forthecrown.core.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Collection;
import java.util.Set;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.events.ChannelMessageEvent;
import net.forthecrown.text.Messages;
import net.forthecrown.user.User;
import net.forthecrown.user.UserBlockList;
import net.forthecrown.user.Users;
import net.forthecrown.user.event.UserAfkEvent;
import net.forthecrown.utils.Audiences;
import net.kyori.adventure.audience.Audience;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class IgnoreListListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onChannelMessage(ChannelMessageEvent event) {
    User user = event.getUserSource();

    if (user == null) {
      return;
    }

    Set<Audience> viewers = event.getTargets();

    if (viewers.isEmpty()) {
      return;
    }

    if (viewers.size() == 1
        && !event.isAnnouncement()
        && !user.hasPermission(CorePermissions.IGNORE_BYPASS)
    ) {
      Audience first = viewers.iterator().next();
      User target = Audiences.getUser(first);

      if (target != null) {
        boolean blocked = UserBlockList.testBlocked(
            user, target,
            Messages.BLOCKED_SENDER,
            Messages.BLOCKED_TARGET
        );

        if (blocked) {
          event.setCancelled(true);
        }

        return;
      }
    }

    filter(user, viewers);
  }

  @EventHandler(ignoreCancelled = true)
  public void onAsyncChat(AsyncChatEvent event) {
    filter(Users.get(event.getPlayer()), event.viewers());
  }

  @EventHandler(ignoreCancelled = true)
  public void onUserAfk(UserAfkEvent event) {
    event.addFilter(user -> !UserBlockList.areBlocked(user, event.getUser()));
  }

  static void filter(User user, Collection<Audience> viewers) {
    if (user.hasPermission(CorePermissions.IGNORE_BYPASS)) {
      return;
    }

    viewers.removeIf(audience -> {
      User viewer = Audiences.getUser(audience);

      if (viewer == null) {
        return false;
      }

      return UserBlockList.areBlocked(viewer, user);
    });
  }
}
