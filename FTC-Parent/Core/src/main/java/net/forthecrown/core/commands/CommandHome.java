package net.forthecrown.core.commands;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.commands.arguments.HomeParseResult;
import net.forthecrown.core.commands.arguments.HomeType;
import net.forthecrown.core.commands.manager.FtcCommand;
import net.forthecrown.core.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.user.UserHomes;
import net.forthecrown.core.user.data.UserTeleport;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;

public class CommandHome extends FtcCommand {
    public static final String DEFAULT = "home";

    public CommandHome(){
        super("home", CrownCore.inst());

        setPermission(Permissions.HOME);
        setDescription("Takes you to one of your homes");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    UserHomes homes = user.getHomes();
                    if(!homes.contains(DEFAULT)) throw FtcExceptionProvider.noDefaultHome();

                    Location l = homes.get(DEFAULT);

                    if(!user.hasPermission(Permissions.WORLD_BYPASS)){
                        if(CommandTpask.isNonAcceptedWorld(l.getWorld())) throw FtcExceptionProvider.badWorldHome(DEFAULT);
                    }

                    user.createTeleport(() -> l, true, UserTeleport.Type.HOME)
                            .setCompleteMessage(Component.text("Teleporting home").color(NamedTextColor.GRAY))
                            .start(true);
                    return 0;
                })

                .then(argument("home", HomeType.home())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            HomeParseResult result = c.getArgument("home", HomeParseResult.class);
                            Location l = result.getHome(c.getSource(), false);

                            if(!user.hasPermission(Permissions.WORLD_BYPASS)){
                                if(CommandTpask.isNonAcceptedWorld(l.getWorld())) throw FtcExceptionProvider.badWorldHome(result.getName());
                            }

                            user.createTeleport(() -> l, true, UserTeleport.Type.HOME)
                                    .setCompleteMessage(Component.text("Teleporting to " + result.getName()).color(NamedTextColor.GRAY))
                                    .start(true);
                            return 0;
                        })
                );
    }
}
