package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.utils.ChatFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;

public class CommandSetSpawn extends FtcCommand {
    public CommandSetSpawn(){
        super("setspawn", CrownCore.inst());

        setPermission(Permissions.CORE_ADMIN);

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("world")
                        .executes(c -> setWorldSpawn(getPlayerSender(c).getLocation(), c.getSource()))

                        .then(argument("loc", PositionArgument.position())
                                .executes(c -> setWorldSpawn(
                                        PositionArgument.getLocation(c, "loc"),
                                        c.getSource()
                                ))
                        )
                )

                .then(literal("server")
                        .executes(c -> setServerSpawn(getPlayerSender(c).getLocation(), c.getSource()))

                        .then(argument("loc", PositionArgument.position())
                                .executes(c -> setServerSpawn(
                                        PositionArgument.getLocation(c, "loc"),
                                        c.getSource()
                                ))
                        )
                );
    }

    private int setServerSpawn(Location l, CommandSource source){
        CrownCore.setServerSpawn(l);

        source.sendMessage(
                Component.text("Set server spawn to ")
                        .color(NamedTextColor.GRAY)
                        .append(ChatFormatter.clickableLocationMessage(l, true).color(NamedTextColor.GOLD))
        );
        return 0;
    }

    private int setWorldSpawn(Location l, CommandSource source){
        l = l.toCenterLocation();
        l.getWorld().setSpawnLocation(l);

        source.sendAdmin(
                Component.text("Set world spawn to ")
                        .append(ChatFormatter.clickableLocationMessage(l, true))
        );
        return 0;
    }
}
