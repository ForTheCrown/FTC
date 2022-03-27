package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.arguments.UserParseResult;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;

public class CommandVanish extends FtcCommand {
    public CommandVanish(){
        super("vanish", Crown.inst());

        setPermission(Permissions.VANISH);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> vanish(c.getSource(), getUserSender(c), false))

                .then(argument("user", UserArgument.user())
                        .executes(c -> vanish(
                                        c.getSource(),
                                        c.getArgument("user", UserParseResult.class).getUser(c.getSource(), false),
                                        false
                        ))

                        .then(argument("message", BoolArgumentType.bool())
                                .executes(c -> vanish(
                                        c.getSource(),
                                        c.getArgument("user", UserParseResult.class).getUser(c.getSource(), false),
                                        c.getArgument("message", Boolean.class)
                                ))
                        )
                );
    }

    private int vanish(CommandSource source, CrownUser user, boolean joinLeaveMsg) {
        boolean vanished = user.isVanished();

        if(joinLeaveMsg){
            Component message = vanished ? FtcFormatter.joinMessage(user) : FtcFormatter.leaveMessage(user);
            Crown.getAnnouncer().announceToAllRaw(message);
        }

        user.setVanished(!vanished);
        source.sendAdmin(
                Component.text((vanished ? "Unv" : "V") + "anished ")
                        .append(user.displayName())
        );
        return 0;
    }
}