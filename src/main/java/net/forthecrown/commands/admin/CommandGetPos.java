package net.forthecrown.commands.admin;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;

public class CommandGetPos extends FtcCommand {

  public CommandGetPos() {
    super("getpos");

    setPermission(Permissions.CMD_GET_POS);
    setDescription("Gets the accurate position of a player");
    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("user", Arguments.ONLINE_USER)
            .executes(c -> {
              User user = Arguments.getUser(c, "user");
              Location l = user.getLocation();

              c.getSource().sendMessage(
                  Text.format(
                      """
                      Location of: &e{0, user}&r:
                      x: &e{1}&r
                      y: &e{2}&r
                      z: &e{3}&r
                      yaw: &e{4}&r
                      pitch: &e{5}&r
                      world: '&e{6}&r'""",

                      NamedTextColor.GRAY,

                      user,
                      l.getX(), l.getY(), l.getZ(),
                      l.getYaw(),
                      l.getPitch(),
                      l.getWorld().getName()
                  )
              );

              return 0;
            })
        );
  }
}