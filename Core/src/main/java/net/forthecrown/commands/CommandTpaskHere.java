package net.forthecrown.commands;

import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.actions.TeleportRequest;
import net.forthecrown.user.actions.UserActionHandler;

import static net.forthecrown.commands.CommandTpask.*;

public class CommandTpaskHere extends FtcCommand {

    public CommandTpaskHere(){
        super("tpaskhere", Crown.inst());

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
     * Main Author: Botul
     * Edit by: Wout
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.then(argument("player", UserArgument.onlineUser())
                .executes(c -> {
                    CrownUser player = getUserSender(c);
                    CrownUser target = UserArgument.getUser(c, "player");
                    checkPreconditions(player, target, true);

                    player.sendMessage(cancelRequest(target));
                    target.sendMessage(tpaMessage("tpa.request.here", player));

                    UserActionHandler.handleAction(new TeleportRequest(player, target, true));
                    return 0;
                })
        );
    }
}