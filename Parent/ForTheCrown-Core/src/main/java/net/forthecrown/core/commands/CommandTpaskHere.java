package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.FtcExceptionProvider;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.md_5.bungee.api.ChatColor;

public class CommandTpaskHere extends CrownCommandBuilder {

    public CommandTpaskHere(){
        super("tpaskhere", FtcCore.getInstance());

        setAliases("tpahere", "eptahere", "etpaskhere");
        setDescription("Asks a player to teleport to them.");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Allows players to ask another player to teleport to them.
     * Uses essentials.tpahere, this command just makes it pwetty.
     *
     * Valid usages of command:
     * [tpaskhere, tpahere]
     * - /tpaskhere [player]
     *
     * Permissions used:
     * - ftc.tpahere
     *
     * Referenced other classes:
     * - FtcCore
     * - (Essentials)
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

                    if(target.equals(player)) throw FtcExceptionProvider.create("&7You cannot teleport to yourself");

                    player.sendMessage(CommandTpask.cancelRequest(target.getName()));
                    player.getPlayer().performCommand("essentials:tpahere " + target.getName());

                    //target part
                    TextComponent targetMessage = Component.text(ChatColor.YELLOW + player.getName() + ChatColor.GOLD + " has requested that you teleport to them. ")
                            .append(CommandTpask.ACCEPT_BUTTON)
                            .append(CommandTpask.DENY_BUTTON);

                    target.sendMessage(targetMessage);
                    return 0;
                })
        );
    }
}
