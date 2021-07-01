package net.forthecrown.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.arguments.UserType;
import net.forthecrown.user.CrownUser;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandNickname extends FtcCommand {
    public CommandNickname(){
        super("nickname", CrownCore.inst());

        setAliases("nick");
        setPermission(Permissions.DONATOR_3);
        setDescription("Sets your nickname");

        register();
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
                            else ChatFormatter.checkNickAllowed(nickname);

                            user.setNickname(nickname);

                            user.sendMessage(Component.text(nickname == null ? "Cleared nickname" : "Set nickname to " + nickname).color(NamedTextColor.GRAY));
                            return 0;
                        })

                        .then(argument("user", UserType.user())
                                .requires(s -> s.hasPermission(Permissions.CORE_ADMIN))

                                .executes(c -> {
                                    CrownUser user = UserType.getUser(c, "user");
                                    String nickname = c.getArgument("nick", String.class);

                                    if(nickname.startsWith("-clear")) nickname = null;
                                    else ChatFormatter.checkNickAllowed(nickname);

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