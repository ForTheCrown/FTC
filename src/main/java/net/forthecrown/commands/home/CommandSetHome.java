package net.forthecrown.commands.home;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.tpa.CommandTpask;
import net.forthecrown.core.Permissions;
import net.forthecrown.text.Messages;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.data.UserHomes;
import org.bukkit.Location;

import static net.forthecrown.commands.manager.Exceptions.CANNOT_SET_HOME;

public class CommandSetHome extends FtcCommand {
    public CommandSetHome(){
        super("sethome");

        setPermission(Permissions.HOME);
        setDescription("Sets a home where you're standing");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                // /sethome
                .executes(c -> attemptHomeSetting(new HomeCreationContext(getUserSender(c), UserHomes.DEFAULT)))

                // /sethome <home>
                .then(argument("name", StringArgumentType.word())
                        .executes(c -> attemptHomeSetting(new HomeCreationContext(
                                getUserSender(c),
                                StringArgumentType.getString(c, "name")
                        )))
                );
    }


    private int attemptHomeSetting(HomeCreationContext context) throws CommandSyntaxException {
        boolean contains = context.homes.contains(context.name);

        if(!contains && !context.homes.canMakeMore()) {
            throw Exceptions.overHomeLimit(context.user);
        }

        // Test to make sure the user is allowed to make
        // a home in this world.
        CommandTpask.testWorld(
                context.loc.getWorld(),
                context.user.getPlayer(),
                CANNOT_SET_HOME
        );

        context.homes.set(context.name, context.loc);

        if(context.isDefault) {
            context.user.getPlayer().setBedSpawnLocation(context.loc, true);
            context.user.sendMessage(Messages.HOMES_DEF_SET);
        } else {
            context.user.sendMessage(Messages.homeSet(context.name));
        }

        return 0;
    }

    private static class HomeCreationContext {
        private final User user;
        private final UserHomes homes;
        private final String name;
        private final Location loc;
        private final boolean isDefault;

        HomeCreationContext(User user, String name) {
            this.user = user;
            this.homes = user.getHomes();
            this.name = name;
            this.loc = user.getLocation();
            this.isDefault = name.equals(UserHomes.DEFAULT);
        }
    }
}