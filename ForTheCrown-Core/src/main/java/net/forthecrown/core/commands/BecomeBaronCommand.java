package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.exceptions.CannotAffordTransaction;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class BecomeBaronCommand extends CrownCommand  {
    public BecomeBaronCommand() {
        super("becomebaron", FtcCore.getInstance());
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Explain what command is supposed to be used for..
     *
     *
     * Valid usages of command:
     * - /becomebaron
     * - /becomebaron confirm
     *
     * Permissions used:
     * - NONE
     *
     * Referenced other classes:
     * - FtcCore: FtcCore.getPrefix | FtcCore.getUserData
     * - Balances
     * - Economy: Economy.getBalances
     * - FtcUser: user.isBaron | user.setBaron
     *
     * Author: Botul
     */

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args) throws CrownException {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);
        int baronPrice = FtcCore.getInstance().getConfig().getInt("BaronPrice");
        Player player = (Player) sender;
        CrownUser data = FtcCore.getUser(player.getUniqueId());
        Balances bals = FtcCore.getBalances();

        if(data.isBaron()){
            player.sendMessage("You are already a baron!");
            return false;
        }

        if(bals.getBalance(player.getUniqueId()) < baronPrice) throw new CannotAffordTransaction(sender, "You need at least 500,000 Rhines to be come baron");

        if(args.length == 1 && args[0].contains("confirm")){
            bals.setBalance(player.getUniqueId(), bals.getBalance(player.getUniqueId()) - baronPrice);
            data.setBaron(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Congratulations!&r You are now a &ebaron&r!"));
            System.out.println(player.getName() + " became a baron!");
            return true;
        }
        if(args.length == 1) return true;

        TextComponent confirmBaron = new TextComponent(ChatColor.GREEN + "[Confirm]");
        confirmBaron.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/becomebaron confirm"));
        confirmBaron.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Become a baron.")));

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setGroupingUsed(true);
        decimalFormat.setGroupingSize(3);

        TextComponent baronConfirmMessage = new TextComponent(FtcCore.getPrefix() + ChatColor.translateAlternateColorCodes('&', "&rAre you sure you wish to become a " + Rank.BARON.getPrefix().replaceAll(" ", "") + "? This will cost &e" + decimalFormat.format(baronPrice) + " Rhines "));
        baronConfirmMessage.addExtra(confirmBaron);

        player.spigot().sendMessage(baronConfirmMessage);
        return true;
    }
}
