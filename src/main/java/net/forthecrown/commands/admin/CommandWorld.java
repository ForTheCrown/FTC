package net.forthecrown.commands.admin;

import net.forthecrown.utils.text.Text;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.WorldArgument;
import net.forthecrown.user.User;
import net.forthecrown.user.UserTeleport;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;

public class CommandWorld extends FtcCommand {
    public CommandWorld(){
        super("world");

        setPermission(Permissions.CMD_TELEPORT);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("world", WorldArgument.world())
                        .executes(c -> {
                            User user = getUserSender(c);
                            World world = c.getArgument("world", World.class);

                            user.createTeleport(() -> world.getSpawnLocation().toCenterLocation(), UserTeleport.Type.OTHER)
                                    .setStartMessage(
                                            Component.text("Teleporting to " + world.getName(),
                                                    NamedTextColor.GRAY
                                            )
                                    )
                                    .setDelayed(false)
                                    .start();

                            return 0;
                        })

                        .then(argument("user", Arguments.ONLINE_USER)
                                .executes(c -> {
                                    User user = Arguments.getUser(c, "user");
                                    World world = c.getArgument("world", World.class);

                                    user.createTeleport(() -> world.getSpawnLocation().toCenterLocation(), UserTeleport.Type.OTHER)
                                            .setDelayed(false)
                                            .setSilent(user.hasPermission(Permissions.CMD_TELEPORT))
                                            .start();

                                    c.getSource().sendAdmin(
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