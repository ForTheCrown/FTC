package net.forthecrown.commands;

import net.forthecrown.commands.help.UsageFactory;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import org.bukkit.Location;

public class CommandSetSpawn extends FtcCommand {

  public CommandSetSpawn() {
    super("setspawn");

    setPermission(Permissions.ADMIN);

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    addUsages(factory, "world");
    addUsages(factory, "server");
  }

  private void addUsages(UsageFactory factory, String name) {
    factory = factory.withPrefix(name);

    factory.usage("")
        .addInfo("Sets the %s's spawn to where you are", name);

    factory.usage("<pos: x,y,z>")
        .addInfo("Sets the %s's spawn to <pos>", name);
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(literal("world")
            .executes(c -> setWorldSpawn(c.getSource().asPlayer().getLocation(), c.getSource()))

            .then(argument("loc", ArgumentTypes.position())
                .executes(c -> setWorldSpawn(
                    ArgumentTypes.getLocation(c, "loc"),
                    c.getSource()
                ))
            )
        )

        .then(literal("server")
            .executes(c -> setServerSpawn(c.getSource().asPlayer().getLocation(), c.getSource()))

            .then(argument("loc", ArgumentTypes.position())
                .executes(c -> setServerSpawn(
                    ArgumentTypes.getLocation(c, "loc"),
                    c.getSource()
                ))
            )
        );
  }

  private int setServerSpawn(Location l, CommandSource source) {
    GeneralConfig.setServerSpawn(l);

    source.sendMessage(Messages.serverSpawnSet(l));
    return 0;
  }

  private int setWorldSpawn(Location l, CommandSource source) {
    l = l.toCenterLocation();
    l.getWorld().setSpawnLocation(l);

    source.sendMessage(Messages.worldSpawnSet(l));
    return 0;
  }
}