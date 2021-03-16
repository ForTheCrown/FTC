package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
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
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
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
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command.then(argument("player", StringArgumentType.word())
                .suggests((c, b) -> UserType.listSuggestions(b))

                .executes(c -> {
                    Player player = getPlayerSender(c);

                    String playerName = c.getArgument("player", String.class);
                    Player target = Bukkit.getPlayer(playerName);

                    if(target == null) throw new InvalidPlayerArgumentException(playerName);
                    if(target.equals(player)) throw new CrownCommandException("You cannot teleport to yourself");

                    //sender part
                    TextComponent cancelTPA = Component.text("[✖]");
                    cancelTPA = cancelTPA.color(NamedTextColor.GRAY);
                    cancelTPA = cancelTPA.clickEvent(ClickEvent.runCommand("/tpacancel")).content("[✖]");
                    cancelTPA = cancelTPA.hoverEvent(HoverEvent.showText(Component.text("Cancel teleportation request.")));

                    TextComponent tpaMessage = Component.text(ChatColor.GOLD + "Request sent to " + ChatColor.YELLOW + target.getName() + ChatColor.GOLD + ". ");
                    tpaMessage = tpaMessage.append(cancelTPA);

                    player.sendMessage(tpaMessage);
                    player.performCommand("essentials:tpahere " + target.getName());

                    //target part
                    TextComponent acceptTPA = Component.text("[✔]");
                    acceptTPA = acceptTPA.color(NamedTextColor.YELLOW);
                    acceptTPA = acceptTPA.clickEvent(ClickEvent.runCommand("/tpaccept")).content("[✔]");
                    acceptTPA = acceptTPA.hoverEvent(HoverEvent.showText(Component.text("Accept teleportation request.")));

                    TextComponent denyTpa = Component.text("[✖]");
                    denyTpa = denyTpa.color(NamedTextColor.GRAY);
                    denyTpa = denyTpa.clickEvent(ClickEvent.runCommand("/tpdeny")).content("[✖]");
                    denyTpa = denyTpa.hoverEvent(HoverEvent.showText(Component.text("Deny teleportation request.")));

                    TextComponent targetMessage = Component.text(ChatColor.YELLOW + player.getName() + ChatColor.GOLD + " has requested to teleport that you teleport to them. ");
                    targetMessage = targetMessage.append(acceptTPA);
                    targetMessage = targetMessage.append(Component.text(" "));
                    targetMessage = targetMessage.append(denyTpa);

                    target.sendMessage(targetMessage);
                    return 0;
                })
        );
    }
}
