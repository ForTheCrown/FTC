package net.forthecrown.marriages.listeners;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.forthecrown.marriages.MExceptions;
import net.forthecrown.marriages.MMessages;
import net.forthecrown.marriages.Marriages;
import net.forthecrown.text.Text;
import net.forthecrown.text.UserClickCallback;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class MarriageConfirmation {
  private MarriageConfirmation() {}

  public static final Map<UUID, UUID> waitingPriestConfirmation = new HashMap<>();
  public static final Set<UUID> waitingFinish = new HashSet<>();

  public static ClickEvent WISH_TO_MARRY = ClickEvent.callback((UserClickCallback) user -> {
    var target = getTarget(user);

    user.sendMessage(
        Component.text()
            .append(MMessages.priestTextConfirm(user, target).create(user))
            .append(Component.space())
            .append(confirmPrompt(user))
    );
  }, builder -> builder.uses(-1).lifetime(Duration.ofDays(365)));

  public static ClickEvent CONFIRM_MARRY = ClickEvent.callback((UserClickCallback) user -> {
    var target = getTarget(user);

    if (waitingFinish.remove(target.getUniqueId())) {
      waitingPriestConfirmation.remove(user.getUniqueId());
      waitingPriestConfirmation.remove(target.getUniqueId());

      Marriages.testCanMarry(user, target);
      Marriages.marry(user, target);

      return;
    }

    // Wait for your spouse
    waitingFinish.add(user.getUniqueId());
    user.sendMessage(MMessages.PRIEST_TEXT_WAITING);
  }, builder -> builder.uses(-1).lifetime(Duration.ofDays(365)));

  static Component confirmPrompt(User user) {
    boolean valid;

    if (Marriages.getSpouse(user) != null) {
      valid = false;
    } else {
      valid = waitingPriestConfirmation.containsKey(user.getUniqueId());
    }

    return MMessages.PRIEST_TEXT_CONFIRM
        .color(valid ? NamedTextColor.AQUA : NamedTextColor.GRAY)
        .clickEvent(CONFIRM_MARRY);
  }

  public static void setWaiting(User user, User target) {
    waitingPriestConfirmation.put(user.getUniqueId(), target.getUniqueId());
    waitingPriestConfirmation.put(target.getUniqueId(), user.getUniqueId());
  }

  public static Component initialPrompt(User user) {
    var builder = Component.text();
    builder.append(MMessages.PRIEST_TEXT);
    builder.append(Component.newline());
    builder.append(marryPrompt(user).clickEvent(WISH_TO_MARRY));
    return builder.build();
  }

  private static Component marryPrompt(User user) {
    var spouse = Marriages.getSpouse(user);
    var target = waitingPriestConfirmation.get(user.getUniqueId());

    if (spouse != null || target == null) {
      return MMessages.PRIEST_TEXT_MARRY.color(NamedTextColor.GRAY);
    }

    User targetUser = Users.get(target);
    return Text.vformat("[I wish to marry {0, user}]", NamedTextColor.AQUA, targetUser)
        .create(user);
  }

  static User getTarget(User user) throws CommandSyntaxException {
    UUID target = waitingPriestConfirmation.get(user.getUniqueId());

    var currentSpouse = Marriages.getSpouse(user);
    if (currentSpouse != null) {
      throw MExceptions.senderAlreadyMarried(currentSpouse);
    }

    if (target == null) {
      throw MExceptions.PRIEST_NO_ONE_WAITING;
    }

    return Users.get(target);
  }
}
