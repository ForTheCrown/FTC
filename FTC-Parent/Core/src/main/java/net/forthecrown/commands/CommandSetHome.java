package net.forthecrown.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserHomes;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;

public class CommandSetHome extends FtcCommand {
    public CommandSetHome(){
        super("sethome", CrownCore.inst());

        setPermission(Permissions.HOME);
        setDescription("Sets a home where your standing");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> attemptHomeSetting(new HomeCreationContext(getUserSender(c), CommandHome.DEFAULT)))

                .then(argument("name", StringArgumentType.word())
                        .executes(c -> attemptHomeSetting(new HomeCreationContext(
                                getUserSender(c),
                                StringArgumentType.getString(c, "name")
                        )))
                );
    }


    private int attemptHomeSetting(HomeCreationContext context) throws RoyalCommandException {
        if(context.homes.contains(context.name)) throw FtcExceptionProvider.homeNameInUse();
        if(!context.homes.canMakeMore()) throw FtcExceptionProvider.overHomeLimit(context.user);

        if(!context.user.hasPermission(Permissions.WORLD_BYPASS)){
            if(CommandTpask.isNonAcceptedWorld(context.loc.getWorld())) throw FtcExceptionProvider.cannotSetHomeHere();
        }

        if(context.isDefault) context.user.getPlayer().setBedSpawnLocation(context.loc, true);

        context.homes.set(context.name, context.loc);
        context.user.sendMessage(
                Component.translatable("homes.set", Component.text(context.name).color(NamedTextColor.GOLD))
                        .color(NamedTextColor.YELLOW)
        );
        return 0;
    }

    private static class HomeCreationContext {
        private final CrownUser user;
        private final UserHomes homes;
        private final String name;
        private final Location loc;
        private final boolean isDefault;

        HomeCreationContext(CrownUser user, String name) {
            this.user = user;
            this.homes = user.getHomes();
            this.name = name;
            this.loc = user.getLocation();
            this.isDefault = name.equals(CommandHome.DEFAULT);
        }
    }
}
