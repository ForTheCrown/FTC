package net.forthecrown.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.arguments.UserParseResult;
import net.forthecrown.commands.arguments.UserType;
import net.forthecrown.user.CrownUser;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;

public class CommandVanish extends FtcCommand {
    public CommandVanish(){
        super("vanish", CrownCore.inst());

        setPermission(Permissions.VANISH);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> vanish(c.getSource(), getUserSender(c), false))

                .then(argument("user", UserType.user())
                        .executes(c -> vanish(
                                        c.getSource(),
                                        c.getArgument("user", UserParseResult.class).getUser(c.getSource()),
                                        false
                        ))

                        .then(argument("message", BoolArgumentType.bool())
                                .executes(c -> vanish(
                                        c.getSource(),
                                        c.getArgument("user", UserParseResult.class).getUser(c.getSource()),
                                        c.getArgument("message", Boolean.class)
                                ))
                        )
                );
    }

    private int vanish(CommandSource source, CrownUser user, boolean joinLeaveMsg) throws CommandSyntaxException {
        boolean vanished = user.isVanished();

        if(joinLeaveMsg){
            Component message = vanished ? ChatFormatter.joinMessage(user) : ChatFormatter.formatLeaveMessage(user);
            CrownCore.getAnnouncer().announceToAllRaw(message);
        }

        user.setVanished(!vanished);
        source.sendAdmin(
                Component.text((vanished ? "Unv" : "V") + "anished ")
                        .append(user.displayName())
        );
        return 0;
    }
}
