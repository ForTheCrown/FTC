package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.forthecrown.core.commands.brigadier.types.TargetSelectorType;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

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
     * - ftc.tpa
     *
     * Referenced other classes:
     * - FtcCore
     * - (Essentials)
     *
     * Main Author: Botul
     * Edit by: Wout
     */

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command.then(argument("player", UserType.onlinePlayer())
                .suggests((c, b) -> UserType.listSuggestions(b))

                .executes(c -> {
                    Player player = getPlayerSender(c);

                    Player target = TargetSelectorType.getPlayer(c, "player");
                    if(target.equals(player)) throw new CrownCommandException("&7You cannot teleport to yourself");

                    //sender part
                    TextComponent tpaMessage = Component.text(ChatColor.GOLD + "Request sent to " + ChatColor.YELLOW + target.getName() + ChatColor.GOLD + ". ")
                            .append(Component.text("[✖]")
                                    .clickEvent(ClickEvent.runCommand("/tpacancel"))
                                    .hoverEvent(HoverEvent.showText(Component.text("Cancel teleportation request."))));

                    player.sendMessage(tpaMessage);
                    player.performCommand("essentials:tpa " + target.getName());

                    //target part
                    TextComponent targetMessage = Component.text(ChatColor.YELLOW + player.getName() + ChatColor.GOLD + " has requested to teleport to you. ")
                            .append(Component.text("[✔] ")
                                    .color(NamedTextColor.YELLOW)
                                    .clickEvent(ClickEvent.runCommand("/tpaccept")).content("[✔]")
                                    .hoverEvent(HoverEvent.showText(Component.text("Accept teleportation request."))))
                            .append(Component.text("[✖]")
                                    .color(NamedTextColor.GRAY)
                                    .clickEvent(ClickEvent.runCommand("/tpdeny")).content("[✖]")
                                    .hoverEvent(HoverEvent.showText(Component.text("Deny teleportation request."))));

                    target.sendMessage(targetMessage);
                    return 0;
                })
        );
    }
}
