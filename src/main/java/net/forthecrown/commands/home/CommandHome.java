package net.forthecrown.commands.home;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.HomeParseResult;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.tpa.CommandTpask;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionAccess;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.Regions;
import net.forthecrown.regions.visit.RegionVisit;
import net.forthecrown.text.Messages;
import net.forthecrown.user.User;
import net.forthecrown.user.UserTeleport;
import net.forthecrown.user.data.UserHomes;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;

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
                    var regions = RegionManager.get();

                    //Check if they have home pole, and if they're in the correct world
                    if (homes.getHomeRegion() != null
                            && user.getWorld().equals(regions.getWorld())
                    ) {
                        RegionAccess local = regions.getAccess(user.getRegionPos());
                        PopulationRegion region = regions.get(homes.getHomeRegion());

                        //If they're close to pole, tp them to home pole
                        if (Regions.isCloseToPole(local.getPolePosition(), user)) {
                            RegionVisit.visitRegion(user, region);
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
        Pair<String, Location> home = result.getHome(source, false);
        var l = home.getValue();

        CommandTpask.testWorld(
                l.getWorld(),
                user.getPlayer(),
                Exceptions.badWorldHome(result.getName())
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