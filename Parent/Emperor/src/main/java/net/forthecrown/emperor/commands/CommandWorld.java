package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.data.UserTeleport;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.WorldArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;

public class CommandWorld extends CrownCommandBuilder {
    public CommandWorld(){
        super("world", CrownCore.inst());

        setPermission(Permissions.HELPER);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("world", WorldArgument.world())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            World world = c.getArgument("world", World.class);

                            user.createTeleport(() -> world.getSpawnLocation().toCenterLocation(), true, true, UserTeleport.Type.OTHER)
                                    .setStartMessage(Component.text("Teleporting to " + world.getName()).color(NamedTextColor.GRAY))
                                    .start(true);
                            return 0;
                        })

                        .then(argument("user", UserType.onlineUser())
                                .executes(c -> {
                                    CrownUser user = UserType.getUser(c, "user");
                                    World world = c.getArgument("world", World.class);

                                    user.createTeleport(() -> world.getSpawnLocation().toCenterLocation(), true, true, UserTeleport.Type.OTHER).start(true);
                                    c.getSource().sendAdmin(
                                            Component.text("Teleporting ")
                                                    .append(user.nickDisplayName())
                                                    .append(Component.text(" to " + world.getName()))
                                    );
                                    return 0;
                                })
                        )
                );
    }
}