package net.forthecrown.commands.home;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.HomeParseResult;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.data.UserHomes;
import org.bukkit.Location;
import org.bukkit.Sound;

public class CommandDeleteHome extends FtcCommand {
    public CommandDeleteHome(){
        super("deletehome");

        setPermission(Permissions.HOME);
        setDescription("Deletes a home");
        setAliases("removehome", "remhome", "yeethome", "delhome");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                // /deletehome
                .executes(c -> {
                    User user = getUserSender(c);
                    UserHomes homes = user.getHomes();

                    if(!homes.contains(UserHomes.DEFAULT)) {
                        throw Exceptions.NO_DEF_HOME;
                    }

                    return delHome(c.getSource(), HomeParseResult.DEFAULT);
                })

                // /deletehome <home>
                .then(argument("home", Arguments.HOME)
                        .executes(c -> {
                            HomeParseResult result = c.getArgument("home", HomeParseResult.class);
                            return delHome(c.getSource(), result);
                        })
                );
    }

    private int delHome(CommandSource source, HomeParseResult result) throws CommandSyntaxException {
        // Because of how the HomeParseResult works, we need to actually
        // get the home location for it to check permissions, because you
        // might've inputted 'JulieWoolie:home' or something
        Pair<String, Location> h = result.get(source, true);
        var name = h.getFirst();

        User user;

        if (result.getUser() == null) {
            user = Users.get(source.asPlayer());
        } else {
            user = Users.get(result.getUser());
        }

        boolean self = source.textName().equals(user.getName());
        var homes = user.getHomes();

        homes.remove(name);

        if (self) {
            user.sendMessage(Messages.deletedHomeSelf(name));
        } else {
            source.sendAdmin(Messages.deletedHomeOther(user, name));
        }

        user.playSound(Sound.UI_TOAST_IN, 2, 1.3f);

        return 0;
    }
}