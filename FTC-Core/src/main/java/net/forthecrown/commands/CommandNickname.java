package net.forthecrown.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandNickname extends FtcCommand {
    public CommandNickname(){
        super("nickname", Crown.inst());

        setAliases("nick");
        setPermission(Permissions.DONATOR_3);
        setDescription("Sets your nickname");

        register();
    }

    /**
     * Checks if the given nickname is allowed
     * @param nick The nickname to check for
     * @throws CommandSyntaxException If the nickname is invalid
     */
    public static void checkNickAllowed(String nick) throws CommandSyntaxException {
        if(FtcVars.maxNickLength.get() < nick.length()) throw FtcExceptionProvider.nickTooLong(nick.length());

        UserCache cache = Crown.getUserManager().getCache();
        UserCache.CacheEntry entry = cache.get(nick);

        if(entry != null) {
            throw FtcExceptionProvider.create("Nickname cannot be the name of another player");
        }
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("nick", StringArgumentType.word())
                        .suggests(suggestMatching("-clear"))

                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            String nickname = c.getArgument("nick", String.class);

                            if(nickname.startsWith("-clear")) nickname = null;
                            else checkNickAllowed(nickname);

                            if(user.hasNickname() && user.getNickname().equals(nickname)) {
                                throw FtcExceptionProvider.create("This is already your nickname");
                            }

                            user.setNickname(nickname);

                            user.sendMessage(Component.text(nickname == null ? "Cleared nickname" : "Set nickname to " + nickname).color(NamedTextColor.GRAY));
                            return 0;
                        })

                        .then(argument("user", UserArgument.user())
                                .requires(s -> s.hasPermission(Permissions.ADMIN))

                                .executes(c -> {
                                    CrownUser user = UserArgument.getUser(c, "user");
                                    String nickname = c.getArgument("nick", String.class);

                                    if(nickname.startsWith("-clear")) nickname = null;
                                    else checkNickAllowed(nickname);

                                    if(user.hasNickname() && user.getNickname().equals(nickname)) {
                                        throw FtcExceptionProvider.create("This is already their nickname");
                                    }

                                    user.setNickname(nickname);

                                    c.getSource().sendMessage(
                                            nickname == null ?
                                                    Component.text("Cleared ")
                                                            .append(user.displayName())
                                                            .append(Component.text("'s nickname"))
                                                    :
                                                    Component.text("Set ")
                                                            .append(user.displayName())
                                                            .append(Component.text("'s nickname to "))
                                                            .append(user.nickDisplayName())
                                    );
                                    return 0;
                                })
                        )
                );
    }
}
