package net.forthecrown.commands.user;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.help.UsageFactory;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.GrenadierCommand;
import org.bukkit.permissions.Permission;

public class UserCommands {

  static final Permission PERMISSION = Permissions.ADMIN;
  static final String USER_ARG_NAME = "user";

  static final UserCommandNode[] NODES = {
      new UserTimeNode(),
      new UserTitlesNode(),
      new UserCosmeticsNode(),
      new UserEarningsNode(),
      new UserTabNode(),
      new UserAltNode()
  };

  public static void createCommands() {
    new UserCommand().register();
  }

  static class UserCommand extends FtcCommand {

    public UserCommand() {
      super("ftcuser");

      setAliases("users", "user");
      setPermission(PERMISSION);
    }

    @Override
    public void populateUsages(UsageFactory factory) {
      var prefixed = factory.withPrefix("<user>");
      for (var n: NODES) {
        n.createUsages(prefixed.withPrefix(n.argumentName));
      }
    }

    @Override
    public void createCommand(GrenadierCommand command) {
      var argument = argument(USER_ARG_NAME, Arguments.USER);
      UserProvider provider = c -> Arguments.getUser(c, USER_ARG_NAME);

      for (var n : NODES) {
        n.register();

        var literal = literal(n.argumentName);
        n.create(literal, provider);

        argument.then(literal);
      }

      command.then(argument);
    }
  }
}