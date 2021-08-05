package net.forthecrown.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.UserTeleport;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.WorldArgument;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;

public class CommandTeleportExact extends FtcCommand {
    public CommandTeleportExact(){
        super("tp_exact", ForTheCrown.inst());

        setPermission(Permissions.HELPER);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("pos", PositionArgument.position())
                        .then(argument("pitch", FloatArgumentType.floatArg())
                                .then(argument("yaw", FloatArgumentType.floatArg())
                                        .then(argument("world", WorldArgument.world())
                                                .executes(c -> {
                                                    CrownUser user = getUserSender(c);
                                                    World world = c.getArgument("world", World.class);

                                                    Location loc = PositionArgument.getLocation(c, "pos");

                                                    float pitch = c.getArgument("pitch", Float.class);
                                                    float yaw = c.getArgument("yaw", Float.class);

                                                    loc.setWorld(world);
                                                    loc.setPitch(pitch);
                                                    loc.setYaw(yaw);

                                                    if(user.isTeleporting()) throw FtcExceptionProvider.create("Already teleporting");

                                                    user.createTeleport(() -> loc, true, true, UserTeleport.Type.TELEPORT)
                                                            .start(true);

                                                    c.getSource().sendAdmin(
                                                            Component.text("Teleported ")
                                                                    .append(user.nickDisplayName().color(NamedTextColor.YELLOW))
                                                                    .append(Component.text(" to "))
                                                                    .append(FtcFormatter.clickableLocationMessage(loc, false).color(NamedTextColor.YELLOW))
                                                    );
                                                    return 0;
                                                })
                                        )
                                )
                        )
                );
    }
}
