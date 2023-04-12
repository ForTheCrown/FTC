package net.forthecrown.commands.admin;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.UserParseResult;
import net.forthecrown.commands.help.UsageFactory;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.events.player.PlayerJoinListener;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.property.Properties;
import net.kyori.adventure.text.Component;

public class CommandVanish extends FtcCommand {

  public CommandVanish() {
    super("vanish");

    setPermission(Permissions.VANISH);
    setAliases("v");
    setDescription("Makes you or another player vanish");

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   * Allows you to disappear
   *
   * Valid usages of command:
   * - /vanish
   * - /vanish -joinLeaveMessage
   * - /vanish <user>
   * - /vanish <user> -joinLeaveMessage
   *
   * Author: Julie
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("")
        .addInfo("Silently vanishes/unvanishes you");

    factory.usage("-joinLeaveMessage")
        .addInfo("Vanishes/unvanishes you, and broadcasts a")
        .addInfo("Join/leave message");

    factory.usage("<player>")
        .addInfo("Silently vanishes/unvanishes a <player>");

    factory.usage("<player> -joinLeaveMessage")
        .addInfo("Vanishes/unvanishes a <player>, and")
        .addInfo("broadcasts a Join/leave message");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> vanish(c.getSource(), getUserSender(c), false))

        .then(literal("-joinLeaveMessage")
            .executes(c -> vanish(c.getSource(), getUserSender(c), true))
        )

        .then(argument("user", Arguments.USER)
            .executes(c -> vanish(
                c.getSource(),
                c.getArgument("user", UserParseResult.class).get(c.getSource(), false),
                false
            ))

            .then(literal("-joinLeaveMessage")
                .executes(c -> vanish(
                    c.getSource(),
                    c.getArgument("user", UserParseResult.class).get(c.getSource(), false),
                    true
                ))
            )
        );
  }

  private int vanish(CommandSource source, User user, boolean joinLeaveMsg) {
    boolean vanished = user.get(Properties.VANISHED);

    if (joinLeaveMsg) {
      if (vanished) {
        PlayerJoinListener.sendLoginMessage(user);
      } else {
        PlayerJoinListener.sendLogoutMessage(user);
      }
    }

    user.set(Properties.VANISHED, !vanished);

    source.sendSuccess(
        Component.text((vanished ? "Unv" : "V") + "anished ")
            .append(user.displayName())
    );
    return 0;
  }
}