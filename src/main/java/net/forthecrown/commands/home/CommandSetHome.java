package net.forthecrown.commands.home;

import static net.forthecrown.commands.manager.Exceptions.CANNOT_SET_HOME;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.help.UsageFactory;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.tpa.CommandTpask;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.data.UserHomes;
import org.bukkit.Sound;

public class CommandSetHome extends FtcCommand {

  public CommandSetHome() {
    super("sethome");

    setPermission(Permissions.HOME);
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
    var homes = user.getHomes();
    var location = user.getLocation();

    boolean contains = homes.contains(name);

    if (!contains && !homes.canMakeMore()) {
      throw Exceptions.overHomeLimit(user);
    }

    // Test to make sure the user is allowed to make
    // a home in this world.
    CommandTpask.testWorld(
        location.getWorld(),
        user.getPlayer(),
        CANNOT_SET_HOME
    );

    homes.set(name, location);

    if (name.equals(UserHomes.DEFAULT)) {
      user.getPlayer().setBedSpawnLocation(location, true);
      user.sendMessage(Messages.HOMES_DEF_SET);
    } else {
      user.sendMessage(Messages.homeSet(name));
    }

    user.playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

    return 0;
  }
}