package net.forthecrown.antigrief.commands;


import net.forthecrown.Loggers;
import net.forthecrown.antigrief.GMessages;
import net.forthecrown.antigrief.GriefPermissions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.UserBlockList;
import net.forthecrown.user.UserBlockList.IgnoreResult;
import org.slf4j.Logger;

public class CommandSeparate extends FtcCommand {

  private static final Logger LOGGER = Loggers.getLogger("Separation");

  public CommandSeparate() {
    super("seperate");

    setPermission(GriefPermissions.PUNISH_SEPARATE);
    setDescription("Seperates/unseparates 2 players");

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   * Forces two people to ignore each other
   *
   * Valid usages of command:
   * /seperate <first user> <second user>
   *
   * Permissions used:
   * ftc.police
   *
   * Main Author: Julie
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<user 1> <user 2>", "Separates/unseparates 2 players");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("first", Arguments.USER)
            .then(argument("second", Arguments.USER)
                .executes(c -> {
                  User first = Arguments.getUser(c, "first");
                  User second = Arguments.getUser(c, "second");

                  UserBlockList firstInter = first.getComponent(UserBlockList.class);
                  UserBlockList secondInter = second.getComponent(UserBlockList.class);

                  // Both users' lists separated lists have to contain
                  // the other player, just to make sure
                  if (firstInter.testIgnored(second) == IgnoreResult.SEPARATED
                      && secondInter.testIgnored(first) == IgnoreResult.SEPARATED
                  ) {
                    firstInter.removeSeparated(second);
                    secondInter.removeSeparated(first);

                    c.getSource().sendSuccess(GMessages.unseparating(first, second));

                    LOGGER.info("{} un-separated {} and {}",
                        c.getSource().textName(), first, second
                    );
                  } else {
                    firstInter.setIgnored(second, true);
                    secondInter.setIgnored(first, true);

                    c.getSource().sendSuccess(GMessages.separating(first, second));

                    LOGGER.info("{} separated {} and {}",
                        c.getSource().textName(), first, second
                    );
                  }

                  return 0;
                })
            )
        );
  }
}