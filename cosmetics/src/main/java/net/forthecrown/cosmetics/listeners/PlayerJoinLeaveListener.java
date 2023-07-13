package net.forthecrown.cosmetics.listeners;

import java.util.Set;
import net.forthecrown.cosmetics.CosmeticData;
import net.forthecrown.cosmetics.LoginEffect;
import net.forthecrown.cosmetics.LoginEffects;
import net.forthecrown.text.Messages;
import net.forthecrown.user.NameRenderFlags;
import net.forthecrown.user.User;
import net.forthecrown.user.event.UserJoinEvent;
import net.forthecrown.user.event.UserLeaveEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerJoinLeaveListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onUserJoin(UserJoinEvent event) {

    event.setRenderer((user, viewer) -> {
      var displayName = formattedName(user, viewer, true);

      return event.hasNameChanged()
          ? Messages.newNameJoinMessage(displayName, event.getLastOnlineName())
          : Messages.joinMessage(displayName);
    });
  }

  @EventHandler(ignoreCancelled = true)
  public void onUserLeave(UserLeaveEvent event) {

    event.setRenderer((user, viewer) -> {
      return Messages.leaveMessage(formattedName(user, viewer, false), event.getReason());
    });
  }

  private Component formattedName(User user, Audience viewer, boolean online) {
    Set<NameRenderFlags> flags = online
        ? Set.of(NameRenderFlags.ALLOW_NICKNAME, NameRenderFlags.USER_ONLINE)
        : Set.of(NameRenderFlags.ALLOW_NICKNAME);

    Component displayName = user.displayName(viewer, flags);
    LoginEffect effect = user.getComponent(CosmeticData.class).getValue(LoginEffects.TYPE);

    if (effect == null) {
      return displayName;
    }

    return Component.text()
        .append(effect.prefix())
        .append(displayName)
        .append(effect.suffix())
        .build();
  }
}