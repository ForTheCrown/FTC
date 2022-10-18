package net.forthecrown.commands.admin;

import net.forthecrown.text.Text;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import org.bukkit.Location;

public class CommandGetPos extends FtcCommand {
    public CommandGetPos(){
        super("getpos");

        setPermission(Permissions.CMD_GET_POS);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", Arguments.ONLINE_USER)
                        .executes(c -> {
                            User user = Arguments.getUser(c, "user");
                            Location l = user.getLocation();

                            c.getSource().sendMessage(
                                    Text.format(
                                            """
                                            Location of: {0, user}:
                                            x: {1}
                                            y: {2}
                                            z: {3}
                                            yaw: {4}
                                            pitch: {5}
                                            world: '{6}'
                                            """,

                                            user,
                                            l.getX(), l.getY(), l.getZ(),
                                            l.getPitch(),
                                            l.getYaw(),
                                            l.getYaw()
                                    )
                            );

                            return 0;
                        })
                );
    }
}