package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandStaffChat extends FtcCommand {

    public CommandStaffChat(){
        super("staffchat", Crown.inst());

        setPermission(Permissions.STAFF_CHAT);
        setAliases("sc");
        setDescription("Sends a message to the staff chat");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Sends a message to the staff chat
     *
     *
     * Valid usages of command:
     * - /staffchat
     * - /sc
     *
     * Permissions used:
     * - ftc.staffchat
     *
     * Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.then(argument("message", StringArgumentType.greedyString())
                .suggests((c, b) -> FtcSuggestionProvider.suggestPlayerNamesAndEmotes(c, b, true))

                .executes(c -> {
                    if(c.getSource().isPlayer() && StaffChat.ignoring.contains(c.getSource().asPlayer())){
                        throw FtcExceptionProvider.create("You are ingoring staff chat, do '/sct visible' to use it again");
                    }

                    StaffChat.send(
                            c.getSource(),
                            FtcFormatter.formatString(
                                    c.getArgument("message", String.class),
                                    c.getSource().asBukkit(),
                                    true
                            ),
                            true
                    );
                    return 0;
                })
        );
    }
}