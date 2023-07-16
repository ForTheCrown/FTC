package net.forthecrown.core.commands.admin;

import static net.forthecrown.command.Commands.getUserSender;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Map;
import net.forthecrown.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandData;
import net.forthecrown.grenadier.annotations.VariableInitializer;
import net.forthecrown.text.Messages;
import net.forthecrown.text.format.FormatBuilder;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.forthecrown.user.event.UserJoinEvent;
import net.forthecrown.user.event.UserLeaveEvent;
import net.forthecrown.user.event.UserLogEvent;
import org.bukkit.event.player.PlayerQuitEvent.QuitReason;

@CommandData("file = 'commands/vanish.gcn'")
public class CommandVanish {

  @VariableInitializer
  void createVars(Map<String, Object> variables) {
    variables.put("perm", Permissions.VANISH.getName());
    variables.put("others_perm", Permissions.VANISH_OTHERS.getName());
  }

  void toggleSelf(CommandSource source) throws CommandSyntaxException {
    toggleSelfInternal(source, false);
  }

  void toggleSelfSilent(CommandSource source) throws CommandSyntaxException {
    toggleSelfInternal(source, true);
  }

  void toggleSelfInternal(CommandSource source, boolean silent) throws CommandSyntaxException {
    User user = getUserSender(source);

    boolean newState = !user.get(Properties.VANISHED);
    user.set(Properties.VANISHED, newState);

    if (!silent) {
      joinLeaveMessage(user, newState);
    }

    source.sendSuccess(Messages.toggleMessage("N{1} vanished", newState));
  }

  void toggleOther(CommandSource source, @Argument("other") User other)
      throws CommandSyntaxException
  {
    toggleOtherInternal(source, other, false);
  }

  void toggleOtherSilent(CommandSource source, @Argument("other") User other)
      throws CommandSyntaxException
  {
    toggleOtherInternal(source, other, true);
  }

  void toggleOtherInternal(CommandSource source, User other, boolean silent)
      throws CommandSyntaxException
  {
    if (source.isPlayer() && source.textName().equals(other.getName())) {
      toggleSelfInternal(source, silent);
      return;
    }

    boolean newState = !other.get(Properties.VANISHED);
    other.set(Properties.VANISHED, newState);

    if (!silent) {
      joinLeaveMessage(other, newState);
    }

    source.sendSuccess(
        FormatBuilder.builder()
            .setViewer(source)
            .setFormat("{0, user} is n{1} vanished")
            .setArguments(other, newState ? "ow" : "o longer")
            .asComponent()
    );

    if (other.isOnline() && !other.hasPermission(Permissions.VANISH)) {
      other.sendMessage(Messages.toggleMessage("You are n{1} vanished", newState));
    }
  }

  void joinLeaveMessage(User user, boolean vanished) {
    if (vanished) {
      leaveMessage(user);
    } else {
      joinMessage(user);
    }
  }

  void leaveMessage(User user) {
    UserLeaveEvent event = new UserLeaveEvent(user, QuitReason.DISCONNECTED, true);
    event.callEvent();
    UserLogEvent.maybeAnnounce(event);
  }

  void joinMessage(User user) {
    UserJoinEvent event = new UserJoinEvent(user, user.getName(), false, true);
    event.callEvent();
    UserLogEvent.maybeAnnounce(event);
  }
}
