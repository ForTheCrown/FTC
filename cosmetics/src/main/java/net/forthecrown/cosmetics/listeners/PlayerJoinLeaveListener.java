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
import net.forthecrown.user.name.DisplayIntent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerJoinLeaveListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onUserJoin(UserJoinEvent event) {

    event.setRenderer((user, viewer) -> {
      var displayName = user.displayName(viewer, DisplayIntent.JOIN_LEAVE_MESSAGE);

      Component base = event.hasNameChanged()
          ? Messages.newNameJoinMessage(displayName, event.getLastOnlineName())
          : Messages.joinMessage(displayName);

      return formatMessage(user, base);
    });
  }

  @EventHandler(ignoreCancelled = true)
  public void onUserLeave(UserLeaveEvent event) {

    event.setRenderer((user, viewer) -> {
      var displayName = user.displayName(
          viewer,
          Set.of(NameRenderFlags.ALLOW_NICKNAME),
          DisplayIntent.JOIN_LEAVE_MESSAGE
      );

      Component base = Messages.leaveMessage(displayName, event.getReason());
      return formatMessage(user, base);
    });
  }

  private Component formatMessage(User user, Component baseMessage) {
    LoginEffect effect = user.getComponent(CosmeticData.class).getValue(LoginEffects.TYPE);

    if (effect == null) {
      return baseMessage;
    }

    return LoginEffects.format(effect, baseMessage);
  }
}