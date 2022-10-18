package net.forthecrown.commands;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.text.Messages;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import org.bukkit.Location;

public class CommandSetSpawn extends FtcCommand {
    public CommandSetSpawn() {
        super("setspawn");

        setPermission(Permissions.ADMIN);

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("world")
                        .executes(c -> setWorldSpawn(c.getSource().asPlayer().getLocation(), c.getSource()))

                        .then(argument("loc", PositionArgument.position())
                                .executes(c -> setWorldSpawn(
                                        PositionArgument.getLocation(c, "loc"),
                                        c.getSource()
                                ))
                        )
                )

                .then(literal("server")
                        .executes(c -> setServerSpawn(c.getSource().asPlayer().getLocation(), c.getSource()))

                        .then(argument("loc", PositionArgument.position())
                                .executes(c -> setServerSpawn(
                                        PositionArgument.getLocation(c, "loc"),
                                        c.getSource()
                                ))
                        )
                );
    }

    private int setServerSpawn(Location l, CommandSource source){
        Crown.config().setServerSpawn(l);

        source.sendMessage(Messages.serverSpawnSet(l));
        return 0;
    }

    private int setWorldSpawn(Location l, CommandSource source){
        l = l.toCenterLocation();
        l.getWorld().setSpawnLocation(l);

        source.sendMessage(Messages.worldSpawnSet(l));
        return 0;
    }
}