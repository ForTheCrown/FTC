package net.forthecrown.core.commands.home;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CoreExceptions;
import net.forthecrown.core.CoreMessages;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.core.user.UserHomes;
import net.forthecrown.events.WorldAccessTestEvent;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import org.bukkit.Sound;

public class CommandSetHome extends FtcCommand {

  public CommandSetHome() {
    super("sethome");

    setPermission(CorePermissions.HOME);
    setDescription("Sets a home where you're standing");

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("")
        .addInfo("Sets your default home, named 'home'");

    factory.usage("<home name>")
        .addInfo("Sets a home to where you're standing");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        // /sethome
        .executes(c -> {
          return attemptHomeSetting(getUserSender(c), UserHomes.DEFAULT);
        })

        // /sethome <home>
        .then(argument("name", StringArgumentType.word())
            .executes(c -> attemptHomeSetting(
                getUserSender(c),
                c.getArgument("name", String.class)
            ))
        );
  }


  private int attemptHomeSetting(User user, String name)
      throws CommandSyntaxException
  {
    var homes = user.getComponent(UserHomes.class);
    var location = user.getLocation();

    boolean contains = homes.contains(name);

    if (!contains && !homes.canMakeMore()) {
      throw CoreExceptions.overHomeLimit(user);
    }

    // Test to make sure the user is allowed to make
    // a home in this world.
    WorldAccessTestEvent.testOrThrow(
        user.getPlayer(),
        location.getWorld(),
        CoreExceptions.CANNOT_SET_HOME
    );

    homes.set(name, location);

    if (name.equals(UserHomes.DEFAULT)) {
      user.getPlayer().setBedSpawnLocation(location, true);
      user.sendMessage(CoreMessages.HOMES_DEF_SET);
    } else {
      user.sendMessage(CoreMessages.homeSet(name));
    }

    user.playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

    return 0;
  }
}