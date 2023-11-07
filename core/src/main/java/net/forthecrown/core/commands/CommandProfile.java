package net.forthecrown.core.commands;

import com.mojang.brigadier.Command;
import java.util.Objects;
import java.util.UUID;
import net.forthecrown.Permissions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CoreExceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.forthecrown.user.name.UserNameFactory;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.Component;

public class CommandProfile extends FtcCommand {

  public CommandProfile() {
    super("profile");

    setAliases("user", "playerprofile", "gameprofile");
    setDescription("Displays a user's information");
    setPermission(Permissions.PROFILE);

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   * Shows some basic info about a user.
   *
   * Valid usages of command:
   * - /profile
   * - /profile [player]
   *
   * Author: Julie
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("")
        .addInfo("Shows your profile");

    factory.usage("<player>")
        .addInfo("Shows you a <player>'s profile");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(command(false))
        .then(argument("player", Arguments.USER)
            .executes(command(true))
        );
  }

  public Command<CommandSource> command(boolean userGiven) {
    return c -> {
      CommandSource s = c.getSource();
      User user = userGiven
          ? Arguments.getUser(c, "player")
          : getUserSender(c);

      if (!canView(s, user)) {
        throw CoreExceptions.profilePrivate(user);
      }

      UserNameFactory factory = Users.getService().getNameFactory();
      Component display = factory.formatProfileDisplay(user, s.asBukkit());

      s.sendMessage(display);
      return 0;
    };
  }

  private boolean canView(CommandSource source, User user) {
    if (source.hasPermission(Permissions.PROFILE_BYPASS)) {
      return true;
    }

    if (source.isPlayer()) {
      UUID id = source.asPlayerOrNull().getUniqueId();

      if (Objects.equals(id, user.getUniqueId())) {
        return true;
      }
    }

    return !user.get(Properties.PROFILE_PRIVATE);
  }
}
