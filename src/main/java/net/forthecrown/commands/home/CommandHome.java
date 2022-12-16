package net.forthecrown.commands.home;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.HomeParseResult;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.tpa.CommandTpask;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.UserTeleport;
import net.forthecrown.user.data.UserHomes;
import net.forthecrown.waypoint.Waypoint;
import net.forthecrown.waypoint.Waypoints;
import net.forthecrown.waypoint.visit.WaypointVisit;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

public class CommandHome extends FtcCommand {

    public CommandHome(){
        super(UserHomes.DEFAULT);

        setPermission(Permissions.HOME);
        setDescription("Takes you to one of your homes");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                // No home name given -> use default home
                // /home
                .executes(c -> {
                    User user = getUserSender(c);
                    UserHomes homes = user.getHomes();

                    BoundingBox playerBounds = user.getPlayer().getBoundingBox();
                    Waypoint waypoint = Waypoints.getColliding(user.getPlayer());

                    if (waypoint != null
                            && waypoint.getBounds().overlaps(playerBounds)
                    ) {
                        var home = homes.getHomeTeleport();

                        if (home != null) {
                            WaypointVisit.visit(user, home);
                            return 0;
                        }
                    }

                    //Check if they have default home
                    if (!homes.contains(UserHomes.DEFAULT)) {
                        throw Exceptions.NO_DEF_HOME;
                    }

                    return goHome(c.getSource(), user, HomeParseResult.DEFAULT);
                })

                // Home name given
                // /home <name>
                .then(argument("home", Arguments.HOME)
                        .executes(c -> {
                            User user = getUserSender(c);
                            HomeParseResult result = c.getArgument("home", HomeParseResult.class);
                            return goHome(c.getSource(), user, result);
                        })
                );
    }

    private int goHome(CommandSource source, User user, HomeParseResult result) throws CommandSyntaxException {
        Pair<String, Location> home = result.get(source, true);
        var l = home.getSecond();

        CommandTpask.testWorld(
                l.getWorld(),
                user.getPlayer(),
                Exceptions.badWorldHome(home.getFirst())
        );

        var teleport = user.createTeleport(() -> l, UserTeleport.Type.HOME);

        if (result.isDefaultHome()) {
            teleport.setCompleteMessage(Messages.TELEPORTING_HOME);
        } else {
            teleport.setCompleteMessage(Messages.teleportingHome(result.getName()));
        }

        teleport.start();
        return 0;
    }
}