package net.forthecrown.core.commands.admin;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.user.UserTeleport.Type;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;

public class CommandWorld extends FtcCommand {

  public CommandWorld() {
    super("world");

    setPermission(CorePermissions.CMD_TELEPORT);
    setDescription("Teleports you or another player into a world");

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<world>", "Teleports you into a <world>");
    factory.usage("<world> <user>", "Teleports a <user> into a <world>");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("world", ArgumentTypes.world())
            .executes(c -> {
              User user = getUserSender(c);
              World world = c.getArgument("world", World.class);

              user.createTeleport(() -> world.getSpawnLocation().toCenterLocation(), Type.OTHER)
                  .setStartMessage(
                      Component.text("Teleporting to " + world.getName(),
                          NamedTextColor.GRAY
                      )
                  )
                  .setDelay(null)
                  .start();

              return 0;
            })

            .then(argument("user", Arguments.ONLINE_USER)
                .executes(c -> {
                  User user = Arguments.getUser(c, "user");
                  World world = c.getArgument("world", World.class);

                  user.createTeleport(() -> world.getSpawnLocation().toCenterLocation(), Type.OTHER)
                      .setDelay(null)
                      .setSilent(user.hasPermission(CorePermissions.CMD_TELEPORT))
                      .start();

                  c.getSource().sendSuccess(
                      Text.format("Teleporting {0, user} to {1}",
                          user, world.getName()
                      )
                  );
                  return 0;
                })
            )
        );
  }
}