package net.forthecrown.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.Vars;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.user.UserLookup;
import net.forthecrown.user.UserLookupEntry;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public class CommandNickname extends FtcCommand {
    public static final String CLEAR = "-clear";

    public CommandNickname(){
        super("nickname");

        setAliases("nick");
        setPermission(Permissions.CMD_NICKNAME);
        setDescription("Sets your nickname");

        register();
    }

    /**
     * Checks if the given nickname is allowed
     * @param nick The nickname to check for
     * @throws CommandSyntaxException If the nickname is invalid
     */
    public static void checkNickAllowed(String nick) throws CommandSyntaxException {
        if (Vars.maxNickLength < nick.length()) {
            throw Exceptions.nickTooLong(nick.length());
        }

        UserLookup cache = UserManager.get().getUserLookup();
        UserLookupEntry entry = cache.get(nick);

        if(entry != null) {
            throw Exceptions.NICK_UNAVAILABLE;
        }
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                // /nick -> clears nick
                .executes(c -> {
                    User user = getUserSender(c);
                    user.setNickname(null);

                    user.sendMessage(Messages.NICK_CLEARED);
                    return 0;
                })

                // /nick <nickname> -> sets nickname or clears it
                .then(argument("nick", StringArgumentType.word())
                        .suggests(suggestMatching(CLEAR))

                        .executes(c -> {
                            User user = getUserSender(c);
                            String nickname = c.getArgument("nick", String.class);
                            checkNickAllowed(nickname);

                            Component nick = format(nickname, user.getPlayer());

                            if (nick == null) {
                                user.sendMessage(Messages.NICK_CLEARED);
                            } else {
                                if (nick.equals(user.getNickname())) {
                                    throw Exceptions.ALREADY_YOUR_NICK;
                                }

                                user.sendMessage(Messages.nickSetSelf(nick));
                            }

                            user.setNickname(nick);
                            return 0;
                        })

                        // /nick <nickname> <user> -> sets/clears nickname for given user
                        .then(argument("user", Arguments.USER)
                                .requires(s -> s.hasPermission(Permissions.ADMIN))

                                .executes(c -> {
                                    User user = Arguments.getUser(c, "user");
                                    String nickname = c.getArgument("nick", String.class);
                                    checkNickAllowed(nickname);

                                    Component nick = format(nickname, c.getSource().asBukkit());
                                    boolean self = c.getSource().textName().equals(user.getName());

                                    if (nick == null) {
                                        c.getSource().sendAdmin(Messages.nickClearOther(user));

                                        if (user.isOnline() && !self) {
                                            user.sendMessage(Messages.NICK_CLEARED);
                                        }
                                    } else {
                                        if (nick.equals(user.getNickname())) {
                                            throw Exceptions.ALREADY_THEIR_NICK;
                                        }

                                        c.getSource().sendAdmin(Messages.nickSetOther(user, nick));

                                        if (user.isOnline() && !self) {
                                            user.sendMessage(Messages.nickSetSelf(nick));
                                        }
                                    }

                                    user.setNickname(nick);
                                    return 0;
                                })
                        )
                );
    }

    private Component format(String nick, CommandSender sender) {
        if (nick.startsWith(CLEAR)) {
            return null;
        }

        return Text.renderString(sender, nick);
    }
}