package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.text.Text;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.WorldArgument;
import net.forthecrown.grenadier.types.args.ArgsArgument;
import net.forthecrown.grenadier.types.args.Argument;
import net.forthecrown.grenadier.types.args.ParsedArgs;
import net.forthecrown.user.UserTeleport;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class CommandTeleportExact extends FtcCommand {
    public static final Argument<World> WORLD = Argument.of("world", WorldArgument.world());

    public static final Argument<Double>
        CORD_X = Argument.of("x", DoubleArgumentType.doubleArg()),
        CORD_Y = Argument.of("y", DoubleArgumentType.doubleArg()),
        CORD_Z = Argument.of("z", DoubleArgumentType.doubleArg());

    public static final Argument<Float>
        YAW = Argument.of("yaw", FloatArgumentType.floatArg(-180, 180), 0f),
        PITCH = Argument.of("pitch", FloatArgumentType.floatArg(-90, 90), 0f);

    public static final ArgsArgument ARGS = ArgsArgument.builder()
            .addRequired(WORLD)

            .addRequired(CORD_X)
            .addRequired(CORD_Y)
            .addRequired(CORD_Z)

            .addOptional(YAW)
            .addOptional(PITCH)

            .build();

    public CommandTeleportExact(){
        super("tp_exact");

        setPermission(Permissions.CMD_TELEPORT);
        register();
    }

    public static ClickEvent createLocationClick(Location location) {
        return Text.argJoiner("/tp_exact")
                .add(WORLD, location.getWorld().getName())

                .add(CORD_X, formatCord(location.getX()))
                .add(CORD_Y, formatCord(location.getY()))
                .add(CORD_Z, formatCord(location.getZ()))

                .add(YAW, formatCord(location.getYaw()))
                .add(PITCH, formatCord(location.getPitch()))

                .joinClickable();
    }

    private static String formatCord(double pos) {
        return String.format("%.02f", pos);
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("args", ARGS)
                        .executes(c -> {
                            var entity = c.getSource().as(Entity.class);
                            var args = c.getArgument("args", ParsedArgs.class);

                            Location loc = new Location(
                                    args.get(WORLD),
                                    args.get(CORD_X),
                                    args.get(CORD_Y),
                                    args.get(CORD_Z),
                                    args.get(YAW),
                                    args.get(PITCH)
                            );

                            if (entity instanceof Player player) {
                                var user = Users.get(player);

                                if (!user.checkTeleporting()) {
                                    return 0;
                                }

                                user.createTeleport(() -> loc, UserTeleport.Type.TELEPORT)
                                        .setDelayed(false)
                                        .setSilent(true)
                                        .start();
                            } else {
                                entity.teleport(loc);
                            }

                            return 0;
                        })
                );
    }
}