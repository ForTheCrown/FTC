package net.forthecrown.commands.tpa;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.TeleportRequest;
import net.forthecrown.user.User;

import static net.forthecrown.commands.tpa.CommandTpask.*;
import static net.forthecrown.text.Messages.*;

public class CommandTpaskHere extends FtcCommand {

    public CommandTpaskHere(){
        super("tpaskhere");

        setAliases("tpahere", "eptahere", "etpaskhere");
        setDescription("Asks a player to teleport to them.");
        setPermission(Permissions.TPA_HERE);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Allows players to ask another player to teleport to them.
     *
     * Valid usages of command:
     * - /tpaskhere <player>
     *
     * Permissions used:
     * - ftc.tpahere
     *
     * Main Author: Julie
     * Edit by: Wout
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.then(argument("player", Arguments.ONLINE_USER)
                .executes(c -> {
                    User player = getUserSender(c);
                    User target = Arguments.getUser(c, "player");
                    checkPreconditions(player, target, true);

                    player.sendMessage(requestSent(target, tpaCancelButton(target)));
                    target.sendMessage(tpaTargetMessage(TPA_FORMAT_HERE, player));

                    TeleportRequest.run(player, target, true);
                    return 0;
                })
        );
    }
}