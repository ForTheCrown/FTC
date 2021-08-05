package net.forthecrown.commands;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;

public class CommandGetPos extends FtcCommand {
    public CommandGetPos(){
        super("getpos", ForTheCrown.inst());

        setPermission(Permissions.HELPER);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserArgument.onlineUser())
                        .executes(c -> {
                            CrownUser user = UserArgument.getUser(c, "user");
                            Location l = user.getLocation();

                            c.getSource().sendMessage(
                                    Component.text()
                                            .append(Component.text("x: " + l.getX()))
                                            .append(Component.newline())

                                            .append(Component.text("y: " + l.getY()))
                                            .append(Component.newline())

                                            .append(Component.text("z: " + l.getZ()))
                                            .append(Component.newline())

                                            .append(Component.text("yaw: " + l.getYaw()))
                                            .append(Component.newline())

                                            .append(Component.text("pitch: " + l.getPitch()))
                                            .append(Component.newline())

                                            .append(Component.text("world: " + l.getWorld().getName()))
                                            .append(Component.newline())

                                            .build()
                            );
                            return 0;
                        })
                );
    }
}
