package net.forthecrown.core.commands;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.commands.arguments.UserType;
import net.forthecrown.core.commands.manager.FtcCommand;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.user.data.TeleportRequest;
import net.forthecrown.grenadier.command.BrigadierCommand;

import static net.forthecrown.core.commands.CommandTpask.*;

public class CommandTpaskHere extends FtcCommand {

    public CommandTpaskHere(){
        super("tpaskhere", CrownCore.inst());

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
        command.then(argument("player", UserType.onlineUser())
                .executes(c -> {
                    CrownUser player = getUserSender(c);
                    CrownUser target = UserType.getUser(c, "player");
                    checkPreconditions(player, target, true);

                    player.sendMessage(cancelRequest(target));
                    target.sendMessage(tpaMessage("tpa.request.here", player));

                    player.getInteractions().handleTeleport(new TeleportRequest(player, target, true));
                    return 0;
                })
        );
    }
}
