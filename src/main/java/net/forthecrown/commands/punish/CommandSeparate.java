package net.forthecrown.commands.punish;

import static net.forthecrown.core.logging.Loggers.STAFF_LOG;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.data.UserInteractions;
import org.apache.logging.log4j.Logger;

public class CommandSeparate extends FtcCommand {

  private static final Logger LOGGER = Loggers.getLogger("Separation");

  public CommandSeparate() {
    super("seperate");

    setPermission(Permissions.PUNISH_SEPARATE);
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

                  UserInteractions firstInter = first.getInteractions();
                  UserInteractions secondInter = second.getInteractions();

                  // Both users' lists separated lists have to contain
                  // the other player, just to make sure
                  if (firstInter.isSeparatedPlayer(second.getUniqueId())
                      && secondInter.isSeparatedPlayer(first.getUniqueId())
                  ) {
                    firstInter.removeSeparated(second.getUniqueId());
                    secondInter.removeSeparated(first.getUniqueId());

                    c.getSource().sendSuccess(Messages.unseparating(first, second));

                    LOGGER.info(STAFF_LOG, "{} un-separated {} and {}",
                        c.getSource().textName(), first, second
                    );
                  } else {
                    firstInter.addSeparated(second.getUniqueId());
                    secondInter.addSeparated(first.getUniqueId());

                    c.getSource().sendSuccess(Messages.separating(first, second));

                    LOGGER.info(STAFF_LOG, "{} separated {} and {}",
                        c.getSource().textName(), first, second
                    );
                  }

                  return 0;
                })
            )
        );
  }
}