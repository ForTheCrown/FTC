package net.forthecrown.core.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpaskCommand implements CommandExecutor {

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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Checks if sender is a player.
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can do this.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 1 || args[0].equalsIgnoreCase(sender.getName())) return false;

        Player target;
        try {
            target = Bukkit.getPlayer(args[0]);
        }
        catch (Exception e) {
            player.sendMessage(ChatColor.GRAY + args[0] + " is not online.");
            return true;
        }

        // Tpa & sender message.
        TextComponent cancelTPA = new TextComponent(ChatColor.GRAY + "[✖]");
        cancelTPA.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpacancel"));
        cancelTPA.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Cancel teleportation request.")));

        TextComponent sentRequest = new TextComponent(ChatColor.GOLD + "Request sent to " + ChatColor.YELLOW + target.getName() + ChatColor.GOLD + ". ");
        sentRequest.addExtra(cancelTPA);
        player.spigot().sendMessage(sentRequest);
        player.performCommand("essentials:tpa " + target.getName());

        // Receiver message.
        TextComponent acceptTPA = new TextComponent(ChatColor.YELLOW + "[✔]");
        acceptTPA.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"));
        acceptTPA.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Accept teleportation request.")));

        TextComponent denyTPA = new TextComponent(ChatColor.GRAY + "[✖]");
        denyTPA.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny"));
        denyTPA.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Deny teleportation request.")));

        TextComponent yayOrNay = new TextComponent(ChatColor.YELLOW + player.getName() + ChatColor.GOLD + " has requested to teleport to you. ");
        yayOrNay.addExtra(acceptTPA);
        yayOrNay.addExtra(" ");
        yayOrNay.addExtra(denyTPA);

        target.spigot().sendMessage(yayOrNay);

        return true;
    }
}
