package net.forthecrown.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.ComponentUtils;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.forthecrown.core.commands.brigadier.types.EntityArgType;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
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
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command.then(argument("player", UserType.onlinePlayer())
                .suggests((c, b) -> UserType.listSuggestions(b))

                .executes(c -> {
                    Player player = getPlayerSender(c);

                    Player target = EntityArgType.getPlayer(c, "player");
                    if(target.equals(player)) throw new CrownCommandException("&7You cannot teleport to yourself");

                    //sender part
                    TextComponent cancelTPA = ComponentUtils.makeComponent("[✖]", NamedTextColor.GRAY,
                            ClickEvent.runCommand("/tpacancel"),
                            HoverEvent.showText(Component.text("Cancel teleportation request.")));

                    TextComponent tpaMessage = Component.text(ChatColor.GOLD + "Request sent to " + ChatColor.YELLOW + target.getName() + ChatColor.GOLD + ". ");
                    tpaMessage = tpaMessage.append(cancelTPA);

                    player.sendMessage(tpaMessage);
                    player.performCommand("essentials:tpa " + target.getName());

                    //target part
                    TextComponent acceptTPA = Component.text("[✔]");
                    acceptTPA = acceptTPA.color(NamedTextColor.YELLOW);
                    acceptTPA = acceptTPA.clickEvent(ClickEvent.runCommand("/tpaccept")).content("[✔]");
                    acceptTPA = acceptTPA.hoverEvent(HoverEvent.showText(Component.text("Accept teleportation request.")));

                    TextComponent denyTpa = Component.text("[✖]");
                    denyTpa = denyTpa.color(NamedTextColor.GRAY);
                    denyTpa = denyTpa.clickEvent(ClickEvent.runCommand("/tpdeny")).content("[✖]");
                    denyTpa = denyTpa.hoverEvent(HoverEvent.showText(Component.text("Deny teleportation request.")));

                    TextComponent targetMessage = Component.text(ChatColor.YELLOW + player.getName() + ChatColor.GOLD + " has requested to teleport to you. ");
                    targetMessage = targetMessage.append(acceptTPA);
                    targetMessage = targetMessage.append(Component.text(" "));
                    targetMessage = targetMessage.append(denyTpa);

                    target.sendMessage(targetMessage);
                    return 0;
                })
        );
    }
}
