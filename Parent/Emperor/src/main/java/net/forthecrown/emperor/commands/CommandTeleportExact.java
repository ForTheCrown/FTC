package net.forthecrown.emperor.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.data.UserTeleport;
import net.forthecrown.emperor.utils.ChatFormatter;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.WorldArgument;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;

public class CommandTeleportExact extends FtcCommand {
    public CommandTeleportExact(){
        super("tp_exact", CrownCore.inst());

        setPermission(Permissions.HELPER);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("pos", PositionArgument.position())
                        .then(argument("pitch", FloatArgumentType.floatArg(-90, 90))
                                .then(argument("yaw", FloatArgumentType.floatArg(-180, 180))
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
                                                                    .append(ChatFormatter.clickableLocationMessage(loc, false).color(NamedTextColor.YELLOW))
                                                    );
                                                    return 0;
                                                })
                                        )
                                )
                        )
                );
    }
}
