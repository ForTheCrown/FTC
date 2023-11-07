package net.forthecrown.core.commands.home;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CoreMessages;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.core.user.UserHomes;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandHomeList extends FtcCommand {

  public CommandHomeList() {
    super("homelist");

    setAliases("homes", "listhomes");
    setPermission(CorePermissions.HOME);
    setDescription("Lists all your homes");

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("", "Lists your homes");

    factory.usage("<user>", "Lists the <user>'s homes")
        .setPermission(CorePermissions.HOME_OTHERS);
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        // /homes
        .executes(c -> {
          User user = getUserSender(c);

          return listHomes(user.getComponent(UserHomes.class), c.getSource(), true);
        })

        // /homes <user>
        .then(argument("user", Arguments.USER)
            .requires(s -> s.hasPermission(CorePermissions.HOME_OTHERS))

            .executes(c -> {
              User user = Arguments.getUser(c, "user");
              boolean self = user.getName().equalsIgnoreCase(c.getSource().textName());

              return listHomes(user.getComponent(UserHomes.class), c.getSource(), self);
            })
        );
  }

  private int listHomes(UserHomes homes, CommandSource source, boolean self)
      throws CommandSyntaxException
  {
    if (homes.isEmpty()) {
      throw Exceptions.NOTHING_TO_LIST;
    }

    var user = homes.getUser();
    var builder = Component.text();

    if (self) {
      builder.append(CoreMessages.HOMES_LIST_HEADER_SELF);
    } else {
      builder.append(CoreMessages.homeListHeader(user));
    }

    if (!CorePermissions.MAX_HOMES.hasUnlimited(user)) {
      int max = user.getComponent(UserHomes.class).getMaxHomes();
      int homeCount = homes.size();

      builder.append(
          Text.format("({0, number} / {1, number})",
              NamedTextColor.YELLOW,
              homeCount, max
          )
      );
    }

    builder.append(Component.text(": ", NamedTextColor.GOLD));

    String prefix = self ? "" : user.getName() + ":";
    builder.append(CoreMessages.listHomes(homes, "/home " + prefix));

    source.sendMessage(builder.build());
    return 0;
  }
}