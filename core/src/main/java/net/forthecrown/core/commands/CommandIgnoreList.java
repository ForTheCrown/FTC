package net.forthecrown.core.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CoreMessages;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.UserBlockList;

public class CommandIgnoreList extends FtcCommand {

  public CommandIgnoreList() {
    super("ignorelist");

    setPermission(CorePermissions.IGNORELIST);
    setDescription("Displays all the ignored players");
    setAliases(
        "blocked", "blockedplayers", "blockedlist",
        "ignoring", "ignored", "ignores",
        "ignoredlist", "ignoredplayers",
        "listignores", "listignored"
    );

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /<command> [user]
   *
   * Permissions used:
   * ftc.commands.ignore
   *
   * Main Author: Julie
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("")
        .addInfo("Shows your ignored players");

    factory.usage("<player>")
        .setPermission(CorePermissions.IGNORELIST_OTHERS)
        .addInfo("Shows a <player>'s ignored players");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> displayIgnored(c.getSource(), getUserSender(c)))

        .then(argument("user", Arguments.USER)
            .requires(s -> s.hasPermission(CorePermissions.IGNORELIST_OTHERS))

            .executes(c -> displayIgnored(c.getSource(), Arguments.getUser(c, "user")))
        );
  }

  private int displayIgnored(CommandSource source, User user) throws CommandSyntaxException {
    UserBlockList list = user.getComponent(UserBlockList.class);

    if (list.getBlocked().isEmpty()) {
      throw Exceptions.NOTHING_TO_LIST;
    }

    source.sendMessage(CoreMessages.listBlocked(list.getBlocked(), source));
    return 0;
  }
}
