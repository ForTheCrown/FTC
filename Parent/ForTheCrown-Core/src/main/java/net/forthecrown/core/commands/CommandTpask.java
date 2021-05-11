package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.FtcExceptionProvider;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;

public class CommandTpask extends CrownCommandBuilder {

    public CommandTpask(){
        super("tpask", FtcCore.getInstance());

        setAliases("tpa", "tprequest", "tpr", "etpa", "etpask");
        setDescription("Asks a to teleport to a player.");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Allows players to teleport to another player by asking them.
     * Uses essentials.tpa, this command just makes it pwetty.
     *
     * Valid usages of command:
     * [tpask, tpa]
     * - /tpask [player]
     *
     * Permissions used:
     * - ftc.commands.tpa
     *
     * Main Author: Botul
     * Edit by: Wout
     */

    static TextComponent cancelRequest(String name){
        return Component.text(ChatColor.GOLD + "Request sent to " + ChatColor.YELLOW + name + ChatColor.GOLD + ". ")
                .append(Component.text("[✖]")
                        .clickEvent(ClickEvent.runCommand("/tpacancel"))
                        .hoverEvent(Component.text("Cancel teleportation request."))
                );
    }

    static final TextComponent ACCEPT_BUTTON = Component.text("[✔] ")
            .color(NamedTextColor.YELLOW)
            .clickEvent(ClickEvent.runCommand("/tpaccept")).content("[✔]")
            .hoverEvent(HoverEvent.showText(Component.text("Accept teleportation request.")));

    static final TextComponent DENY_BUTTON = Component.text("[✖]")
            .color(NamedTextColor.GRAY)
            .clickEvent(ClickEvent.runCommand("/tpdeny")).content("[✖]")
            .hoverEvent(HoverEvent.showText(Component.text("Deny teleportation request.")));

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.then(argument("player", UserType.onlineUser())
                .executes(c -> {
                    CrownUser player = getUserSender(c);
                    CrownUser target = UserType.getUser(c, "player");
                    if(target.equals(player)) throw FtcExceptionProvider.create("&7You cannot teleport to yourself");

                    player.sendMessage(cancelRequest(target.getName()));
                    player.getPlayer().performCommand("essentials:tpa " + target.getName());

                    //target part
                    TextComponent targetMessage = Component.text(ChatColor.YELLOW + player.getName() + ChatColor.GOLD + " has requested to teleport to you. ")
                            .append(ACCEPT_BUTTON)
                            .append(DENY_BUTTON);

                    target.sendMessage(targetMessage);
                    return 0;
                })
        );
    }
}
