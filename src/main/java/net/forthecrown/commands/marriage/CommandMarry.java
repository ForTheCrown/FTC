package net.forthecrown.commands.marriage;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.help.UsageFactory;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.data.UserInteractions;
import net.forthecrown.user.property.Properties;

public class CommandMarry extends FtcCommand {

  public CommandMarry() {
    super("marry");

    setDescription("Marry a person");
    setPermission(Permissions.MARRY);
    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /marry
   *
   * Permissions used:
   * ftc.marry
   *
   * Main Author: Julie
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<player>", "Propose to a <player>");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("user", Arguments.ONLINE_USER)
            .executes(c -> {
              User user = getUserSender(c);
              User target = Arguments.getUser(c, "user");

              UserInteractions userSocials = user.getInteractions();
              UserInteractions targetSocials = target.getInteractions();

              // Not self lol
              if (target.equals(user)) {
                throw Exceptions.MARRY_SELF;
              }

              // Both are unmarried
              if (userSocials.isMarried()) {
                throw Exceptions.ALREADY_MARRIED;
              }

              if (targetSocials.isMarried()) {
                throw Exceptions.targetAlreadyMarried(target);
              }

              // Both accepting proposals
              if (!user.get(Properties.ACCEPTING_PROPOSALS)) {
                throw Exceptions.MARRY_DISABLED_SENDER;
              }

              if (!target.get(Properties.ACCEPTING_PROPOSALS)) {
                throw Exceptions.marriageDisabledTarget(target);
              }

              targetSocials.setLastProposal(user.getUniqueId());

              user.sendMessage(Messages.proposeSender(target));
              target.sendMessage(Messages.proposeTarget(user));
              return 0;
            })
        );
  }
}