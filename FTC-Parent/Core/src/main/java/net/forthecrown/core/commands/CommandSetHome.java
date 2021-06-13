package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.commands.manager.FtcCommand;
import net.forthecrown.core.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.user.UserHomes;
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
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    UserHomes homes = user.getHomes();
                    String name = CommandHome.DEFAULT;
                    Location loc = user.getLocation();

                    if(homes.contains(name)) throw FtcExceptionProvider.homeNameInUse();
                    if(!homes.canMakeMore()) throw FtcExceptionProvider.overHomeLimit(user);

                    if(!user.hasPermission(Permissions.WORLD_BYPASS)){
                        if(CommandTpask.isNonAcceptedWorld(loc.getWorld())) throw FtcExceptionProvider.cannotSetHomeHere();
                    }

                    homes.set(name, loc);
                    user.sendMessage(Component.translatable("homes.setDefault").color(NamedTextColor.YELLOW));
                    return 0;
                })

                .then(argument("name", StringArgumentType.word())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            UserHomes homes = user.getHomes();
                            String name = StringArgumentType.getString(c, "name");
                            Location loc = user.getLocation();

                            if(homes.contains(name)) throw FtcExceptionProvider.homeNameInUse();
                            if(!homes.canMakeMore()) throw FtcExceptionProvider.overHomeLimit(user);

                            if(!user.hasPermission(Permissions.WORLD_BYPASS)){
                                if(CommandTpask.isNonAcceptedWorld(loc.getWorld())) throw FtcExceptionProvider.cannotSetHomeHere();
                            }

                            homes.set(name, loc);
                            user.sendMessage(
                                    Component.translatable("homes.set", Component.text(name).color(NamedTextColor.GOLD))
                                            .color(NamedTextColor.YELLOW)
                            );
                            return 0;
                        })
                );
    }
}
