package net.forthecrown.commands;

import net.forthecrown.commands.arguments.HomeArgument;
import net.forthecrown.commands.arguments.HomeParseResult;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.*;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionAccess;
import net.forthecrown.regions.RegionUtil;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserHomes;
import net.forthecrown.user.UserTeleport;
import net.forthecrown.user.actions.RegionVisitAction;
import net.forthecrown.user.actions.UserActionHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;

public class CommandHome extends FtcCommand {
    public static final String DEFAULT = "home";

    public CommandHome(){
        super(DEFAULT);

        setPermission(Permissions.HOME);
        setDescription("Takes you to one of your homes");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                //No home name given
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    UserHomes homes = user.getHomes();

                    //Check if they have home pole, and if they're in the correct world
                    if(homes.getHomeRegion() != null && user.getWorld().equals(FtcVars.getRegionWorld())) {
                        RegionAccess local = Crown.getRegionManager().getSnapshot(user.getRegionPos());
                        PopulationRegion region = Crown.getRegionManager().get(homes.getHomeRegion());

                        //If they're close to pole, tp them to home pole
                        if(RegionUtil.isCloseToPole(local.getPolePosition(), user)) {
                            RegionVisitAction visit = new RegionVisitAction(user, region);
                            UserActionHandler.handleAction(visit);

                            return 0;
                        }
                    }

                    //Check if they have default home
                    if(!homes.contains(DEFAULT)) throw FtcExceptionProvider.noDefaultHome();

                    Location l = homes.get(DEFAULT);

                    //Invalid world for home
                    if(!user.hasPermission(Permissions.WORLD_BYPASS)) {
                        if(CommandTpask.isInvalidWorld(l.getWorld())) throw FtcExceptionProvider.badWorldHome(DEFAULT);

                        //Don't allow visiting end homes if end closed
                        EndOpener opener = Crown.getEndOpener();
                        if(l.getWorld().equals(Worlds.end()) && opener.isEnabled() && !opener.isOpen()) {
                            throw FtcExceptionProvider.create("The End is currently closed");
                        }
                    }

                    //Teleport them there
                    user.createTeleport(() -> l, true, UserTeleport.Type.HOME)
                            .setCompleteMessage(Component.text("Teleporting home").color(NamedTextColor.GRAY))
                            .start(true);

                    return 0;
                })

                //Home name given
                .then(argument("home", HomeArgument.home())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            HomeParseResult result = c.getArgument("home", HomeParseResult.class);
                            Location l = result.getHome(c.getSource(), false);

                            //Check if the home's world is invalid
                            if(!user.hasPermission(Permissions.WORLD_BYPASS)) {
                                if(CommandTpask.isInvalidWorld(l.getWorld())) throw FtcExceptionProvider.badWorldHome(result.getName());

                                //Don't allow visiting end homes if end closed
                                EndOpener opener = Crown.getEndOpener();
                                if(l.getWorld().equals(Worlds.end()) && opener.isEnabled() && !opener.isOpen()) {
                                    throw FtcExceptionProvider.create("The End is currently closed");
                                }
                            }

                            //Teleport them to home
                            user.createTeleport(() -> l, true, UserTeleport.Type.HOME)
                                    .setCompleteMessage(Component.text("Teleporting to " + result.getName()).color(NamedTextColor.GRAY))
                                    .start(true);
                            return 0;
                        })
                );
    }
}