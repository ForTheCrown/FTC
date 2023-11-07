package net.forthecrown.core.commands.home;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CoreExceptions;
import net.forthecrown.core.CoreMessages;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.core.user.UserHomes;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import org.bukkit.Sound;

public class CommandDeleteHome extends FtcCommand {

  public CommandDeleteHome() {
    super("deletehome");

    setPermission(CorePermissions.HOME);
    setDescription("Deletes a home");
    setAliases("removehome", "remhome", "yeethome", "delhome");

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("")
        .addInfo("Deletes your home named 'home'");

    factory.usage("<home>")
        .addInfo("Deletes <home>");

    factory.usage("<player>:<home>")
        .setPermission(CorePermissions.HOME_OTHERS)
        .addInfo("Deletes a <player>'s <home>");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        // /deletehome
        .executes(c -> {
          User user = getUserSender(c);
          UserHomes homes = user.getComponent(UserHomes.class);

          if (!homes.contains(UserHomes.DEFAULT)) {
            throw CoreExceptions.NO_DEF_HOME;
          }

          return delHome(c.getSource(), HomeParseResult.DEFAULT);
        })

        // /deletehome <home>
        .then(argument("home", HomeArgument.HOME)
            .executes(c -> {
              HomeParseResult result = c.getArgument("home", HomeParseResult.class);
              return delHome(c.getSource(), result);
            })
        );
  }

  private int delHome(CommandSource source, HomeParseResult result) throws CommandSyntaxException {
    // Because of how the HomeParseResult works, we need to actually
    // get the home location for it to check permissions, because you
    // might've inputted 'JulieWoolie:home' or something
    Home h = result.get(source, true);
    var name = h.name();

    User user;

    if (result.getUser() == null) {
      user = Users.get(source.asPlayer());
    } else {
      user = Users.get(result.getUser());
    }

    boolean self = source.textName().equals(user.getName());
    var homes = user.getComponent(UserHomes.class);

    homes.remove(name);

    if (self) {
      user.sendMessage(CoreMessages.deletedHomeSelf(name));
    } else {
      source.sendSuccess(CoreMessages.deletedHomeOther(user, name));
    }

    user.playSound(Sound.UI_TOAST_IN, 2, 1.3f);

    return 0;
  }
}