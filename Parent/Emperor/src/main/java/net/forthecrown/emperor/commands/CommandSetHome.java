package net.forthecrown.emperor.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.UserHomes;
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
