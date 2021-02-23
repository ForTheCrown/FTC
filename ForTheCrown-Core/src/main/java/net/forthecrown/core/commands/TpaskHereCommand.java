package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.InvalidPlayerInArgument;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class TpaskHereCommand extends CrownCommand {

    public TpaskHereCommand(){
        super("tpaskhere", FtcCore.getInstance());

        setAliases("tpahere");
        setDescription("description: Asks a player to teleport to them.");
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
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (!(sender instanceof Player)) throw new NonPlayerExecutor(sender);
        Player player = (Player) sender;

        // Valid command use
        if (args.length < 1 || args[0].equalsIgnoreCase(sender.getName())) return false;

        Player target = Bukkit.getPlayer(args[0]);
        if(target == null) throw new InvalidPlayerInArgument(sender, args[0]);

        // Tpahere & sender message.
        TextComponent cancelTPA = new TextComponent(ChatColor.GRAY + "[✖]");
        cancelTPA.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpacancel"));
        cancelTPA.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Cancel teleportation request.")));

        TextComponent sentRequest = new TextComponent(ChatColor.GOLD + "Request sent to " + ChatColor.YELLOW + target.getName() + ChatColor.GOLD + ". ");
        sentRequest.addExtra(cancelTPA);
        player.spigot().sendMessage(sentRequest);
        player.performCommand("essentials:tpahere " + target.getName());

        // Receiver message.
        TextComponent acceptTPA = new TextComponent(ChatColor.YELLOW + "[✔]");
        acceptTPA.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"));
        acceptTPA.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Accept teleportation request.")));

        TextComponent denyTPA = new TextComponent(ChatColor.GRAY + "[✖]");
        denyTPA.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny"));
        denyTPA.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Deny teleportation request.")));

        TextComponent yayOrNay = new TextComponent(ChatColor.YELLOW + player.getName() + ChatColor.GOLD + " has requested that you teleport to them. ");
        yayOrNay.addExtra(acceptTPA);
        yayOrNay.addExtra(" ");
        yayOrNay.addExtra(denyTPA);

        target.spigot().sendMessage(yayOrNay);

        return true;
    }
}
