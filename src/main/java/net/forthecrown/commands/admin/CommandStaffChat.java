package net.forthecrown.commands.admin;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;

public class CommandStaffChat extends FtcCommand {

    public CommandStaffChat(){
        super("staffchat");

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
        command.then(argument("message", Arguments.CHAT)
                .executes(c -> {
                    StaffChat.send(
                            c.getSource(),
                            c.getArgument("message", Component.class),
                            true
                    );
                    return 0;
                })
        );
    }
}