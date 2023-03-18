package net.forthecrown.commands;

import com.mojang.brigadier.Command;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.UserFormat;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;

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

      UserFormat format = UserFormat.create(user)
          .withViewer(s);

      if (!format.isViewingAllowed()) {
        throw Exceptions.profilePrivate(user);
      }

      TextWriter writer = TextWriters.newWriter();
      UserFormat.applyProfileStyle(writer);
      format.format(writer);

      s.sendMessage(writer.asComponent());
      return 0;
    };
  }
}