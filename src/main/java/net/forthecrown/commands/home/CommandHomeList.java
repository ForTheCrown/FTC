package net.forthecrown.commands.home;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.Messages;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.data.UserHomes;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandHomeList extends FtcCommand {
    public CommandHomeList(){
        super("homelist");

        setAliases("homes", "listhomes");
        setPermission(Permissions.HOME);
        setDescription("Lists all your homes");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                // /homes
                .executes(c -> {
                    User user = getUserSender(c);

                    return listHomes(user.getHomes(), c.getSource(), true);
                })

                // /homes <user>
                .then(argument("user", Arguments.USER)
                        .requires(s -> s.hasPermission(Permissions.HOME_OTHERS))

                        .executes(c -> {
                            User user = Arguments.getUser(c, "user");
                            boolean self = user.getName().equalsIgnoreCase(c.getSource().textName());

                            return listHomes(user.getHomes(), c.getSource(), self);
                        })
                );
    }

    private int listHomes(UserHomes homes, CommandSource source, boolean self) throws CommandSyntaxException {
        if (homes.isEmpty()) {
            throw Exceptions.NOTHING_TO_LIST;
        }

        var user = homes.getUser();
        var builder = Component.text();

        if (self) {
            builder.append(Messages.HOMES_LIST_HEADER_SELF);
        } else {
            builder.append(Messages.homeListHeader(user));
        }

        if (!Permissions.MAX_HOMES.hasUnlimited(user)) {
            int max = Permissions.MAX_HOMES.getTier(user);
            int homeCount = homes.size();

            builder.append(
                    Text.format("({0, number} / {1, number})",
                            NamedTextColor.YELLOW,
                            homeCount, max
                    )
            );
        }

        builder.append(Component.text(": ", NamedTextColor.GOLD));

        String prefix = self ? "" : user.getName() + ":";
        builder.append(Messages.listHomes(homes, "/home " + prefix));

        source.sendMessage(builder.build());
        return 0;
    }
}