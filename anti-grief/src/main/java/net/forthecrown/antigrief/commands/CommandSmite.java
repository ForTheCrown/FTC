package net.forthecrown.antigrief.commands;


import net.forthecrown.Permissions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;

public class CommandSmite extends FtcCommand {

  public CommandSmite() {
    super("smite");

    setPermission(Permissions.ADMIN);
    setDescription("Smites a user lol. This command will deal damage");

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   * Smites a player with lightning
   *
   * Valid usages of command:
   * /smite <player>
   *
   * Permissions used:
   * ftc.admin
   *
   * Main Author: Julie
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<user>", "Smites a <user>");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("user", Arguments.ONLINE_USER)
            .executes(c -> {
              User user = Arguments.getUser(c, "user");

              user.getWorld().strikeLightning(user.getLocation());

              c.getSource().sendMessage(
                  Text.format("Smiting {0, user}.", user)
              );
              return 0;
            })
        );
  }
}