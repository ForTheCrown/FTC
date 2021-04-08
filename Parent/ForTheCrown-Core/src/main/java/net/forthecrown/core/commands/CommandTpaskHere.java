package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.forthecrown.core.commands.brigadier.exceptions.InvalidPlayerArgumentException;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
    protected void registerCommand(BrigadierCommand command) {
        command.then(argument("player", StringArgumentType.word())
                .suggests((c, b) -> UserType.listSuggestions(b))

                .executes(c -> {
                    Player player = getPlayerSender(c);

                    String playerName = c.getArgument("player", String.class);
                    Player target = Bukkit.getPlayer(playerName);

                    if(target == null) throw new InvalidPlayerArgumentException(playerName);
                    if(target.equals(player)) throw new CrownCommandException("You cannot teleport to yourself");

                    //sender part
                    TextComponent tpaMessage = Component.text(ChatColor.GOLD + "Request sent to " + ChatColor.YELLOW + target.getName() + ChatColor.GOLD + ". ")
                            .append(Component.text("[✖]")
                                    .color(NamedTextColor.GRAY)
                                    .clickEvent(ClickEvent.runCommand("/tpacancel")).content("[✖]")
                                    .hoverEvent(HoverEvent.showText(Component.text("Cancel teleportation request."))));

                    player.sendMessage(tpaMessage);
                    player.performCommand("essentials:tpahere " + target.getName());

                    //target part
                    TextComponent targetMessage = Component.text(ChatColor.YELLOW + player.getName() + ChatColor.GOLD + " has requested that you teleport to them. ")
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
