package net.forthecrown.afk;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import net.forthecrown.text.PlayerMessage;
import net.forthecrown.text.Text;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.text.channel.ChannelledMessage;
import net.forthecrown.text.channel.MessageHandler;
import net.forthecrown.user.TimeField;
import net.forthecrown.user.User;
import net.forthecrown.utils.Audiences;
import net.forthecrown.utils.Time;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;

public final class Afk {

  private static final Map<UUID, AfkState> stateMap = new Object2ObjectOpenHashMap<>();

  private static Optional<AfkState> getState(User user) {
    return Optional.ofNullable(stateMap.get(user.getUniqueId()));
  }

  public static boolean isAfk(User user) {
    return getState(user).map(afkState -> afkState.state).orElse(false);
  }

  public static Optional<PlayerMessage> getAfkReason(User user) {
    return getState(user).map(AfkState::getReason);
  }

  public static void setAfk(User user, boolean afk, PlayerMessage reason) {
    AfkState state = stateMap.computeIfAbsent(user.getUniqueId(), uuid -> new AfkState());

    if (afk) {
      if (!state.state) {
        user.setTimeToNow(TimeField.AFK_START);
      }

      state.reason = reason;
    } else {
      if (state.state) {
        logAfkTime(user);
      }

      state.reason = null;
    }

    state.state = afk;
    user.updateTabName();
  }

  private static void logAfkTime(User user) {
    long startTime = user.getTime(TimeField.AFK_START);

    if (startTime != -1) {
      long since = Time.timeSince(startTime);
      long currentValue = user.getTime(TimeField.AFK_TIME);
      user.setTime(TimeField.AFK_TIME, currentValue == -1 ? since : currentValue + since);
    }
  }

  public static void afk(User user, @Nullable PlayerMessage reason) {
    user.ensureOnline();
    Preconditions.checkState(!isAfk(user), "User is already AFK");

    setAfk(user, true, reason);

    ViewerAwareMessage nonNullReason = reason == null
        ? ViewerAwareMessage.wrap(Component.empty())
        : reason;

    ChannelledMessage channelled = ChannelledMessage.create(nonNullReason)
        .setSource(user)
        .setBroadcast()
        .setChannelName("afk");

    channelled.setRenderer((viewer, baseMessage) -> {
      Component displayName;

      if (Audiences.equals(viewer, user)) {
        displayName = Component.text("You are");
      } else {
        displayName = Component.text()
            .append(user.displayName(viewer))
            .append(Component.text(" is"))
            .build();
      }

      Component suffix;

      if (Text.isEmpty(baseMessage)) {
        suffix = Component.text(".");
      } else {
        suffix = Component.text(": ").append(baseMessage);
      }

      return Text.format("{0} now AFK{1}", NamedTextColor.GRAY, displayName, suffix);
    });

    channelled.setHandler(MessageHandler.EMPTY_IF_VIEWER_WAS_REMOVED);
    channelled.send();
  }

  public static void unafk(User user) {
    user.ensureOnline();
    Preconditions.checkState(isAfk(user), "User is not AFK");

    setAfk(user, false, null);

    ChannelledMessage.announce(viewer -> {
      Component displayName;

      if (Audiences.equals(user, viewer)) {
        displayName = Component.text("You are");
      } else {
        displayName = Component.text()
            .append(user.displayName(viewer))
            .append(Component.text(" is"))
            .build();
      }

      return Component.text()
          .color(NamedTextColor.GRAY)
          .append(displayName)
          .append(Component.text(" no longer AFK."))
          .build();
    });
  }

  @Getter
  private static class AfkState {
    boolean state;
    PlayerMessage reason;
  }
}
