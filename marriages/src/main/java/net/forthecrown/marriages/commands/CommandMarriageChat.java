package net.forthecrown.marriages.commands;

import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.marriages.MExceptions;
import net.forthecrown.marriages.MPermissions;
import net.forthecrown.marriages.Marriages;
import net.forthecrown.text.PlayerMessage;
import net.forthecrown.user.User;

public class CommandMarriageChat extends FtcCommand {

  public CommandMarriageChat() {
    super("marriagechat");

    setPermission(MPermissions.MARRY);
    setAliases("marryc", "marriagec", "mc", "mchat");
    setDescription("Chat with your spouse privately");

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   *
   * Permissions used:
   * ftc.marry
   *
   * Main Author: Julie
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<message>", "Chats with your spouse");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("message", Arguments.MESSAGE)

            .executes(c -> {
              User user = getUserSender(c);
              var spouse = Marriages.getSpouse(user);
              PlayerMessage message = Arguments.getPlayerMessage(c, "message");

              if (spouse == null) {
                user.set(Marriages.MCHAT_TOGGLED, false);
                throw MExceptions.NOT_MARRIED;
              }

              if (!spouse.isOnline()) {
                user.set(Marriages.MCHAT_TOGGLED, false);
                throw Exceptions.notOnline(spouse);
              }

              Marriages.mchat(user, message);
              return 0;
            })
        );
  }
}