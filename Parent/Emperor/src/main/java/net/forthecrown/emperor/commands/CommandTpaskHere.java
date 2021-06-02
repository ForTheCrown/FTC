package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.data.TeleportRequest;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import static net.forthecrown.emperor.commands.CommandTpask.*;

public class CommandTpaskHere extends CrownCommandBuilder {

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

                    target.sendMessage(
                            Component.text()
                                    .append(player.nickDisplayName().color(NamedTextColor.YELLOW))
                                    .append(Component.text(" has requested that you teleport to them "))
                                    .append(acceptButton(player))
                                    .append(denyButton(player))
                                    .build()
                    );

                    player.getInteractions().handleTeleport(new TeleportRequest(player, target, true));
                    return 0;
                })
        );
    }
}
