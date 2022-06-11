package net.forthecrown.commands;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.arguments.HomeParseResult;
import net.forthecrown.commands.arguments.HomeArgument;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserHomes;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;

public class CommandDeleteHome extends FtcCommand {
    public CommandDeleteHome(){
        super("deletehome", Crown.inst());

        setPermission(Permissions.HOME);
        setDescription("Deletes a home");
        setAliases("removehome", "remhome", "yeethome", "delhome");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    UserHomes homes = user.getHomes();

                    if(!homes.contains(CommandHome.DEFAULT)) throw FtcExceptionProvider.noDefaultHome();

                    homes.remove(CommandHome.DEFAULT);

                    user.sendMessage(Component.translatable("homes.deleted", Component.text(CommandHome.DEFAULT).color(NamedTextColor.GOLD)).color(NamedTextColor.YELLOW));
                    return 0;
                })

                .then(argument("home", HomeArgument.home())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            UserHomes homes = user.getHomes();
                            HomeParseResult result = c.getArgument("home", HomeParseResult.class);
                            String name = result.getName();

                            // Because of how the HomeParseResult works, we need to actually
                            // get the home location for it to check permissions, because you
                            // might've 'JulieWoolie:home' or something
                            Location l = result.getHome(c.getSource(), false);

                            if(!homes.contains(name)) throw HomeArgument.UNKNOWN_HOME.create(Component.text(name));

                            homes.remove(name);
                            user.sendMessage(Component.translatable("homes.deleted", Component.text(name).color(NamedTextColor.GOLD)).color(NamedTextColor.YELLOW));
                            return 0;
                        })
                );
    }
}