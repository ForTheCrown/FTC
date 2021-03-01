package net.forthecrown.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CannotAffordTransactionException;
import net.forthecrown.core.enums.Rank;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

import java.text.DecimalFormat;

public class BecomeBaronCommand extends CrownCommandBuilder {
    public BecomeBaronCommand() {
        super("becomebaron", FtcCore.getInstance());
        register();
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
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        int baronPrice = FtcCore.getInstance().getConfig().getInt("BaronPrice");
        Balances bals = FtcCore.getBalances();
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setGroupingUsed(true);
        decimalFormat.setGroupingSize(3);

        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);

                    if(user.isBaron()){
                        user.sendMessage("&7You are already a baron!");
                        return 0;
                    }

                    if(bals.getBalance(user.getBase()) < baronPrice) throw new CannotAffordTransactionException("You need at least 500,000 Rhines");

                    TextComponent confirmBaron = new TextComponent(ChatColor.GREEN + "[Confirm]");
                    confirmBaron.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/becomebaron confirm"));
                    confirmBaron.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Become a baron.")));

                    TextComponent baronConfirmMessage = new TextComponent(FtcCore.getPrefix() + ChatColor.translateAlternateColorCodes('&', "&rAre you sure you wish to become a " + Rank.BARON.getPrefix().replaceAll(" ", "") + "? This will cost &e" + decimalFormat.format(baronPrice) + " Rhines "));
                    baronConfirmMessage.addExtra(confirmBaron);

                    user.spigot().sendMessage(baronConfirmMessage);
                    return 0;
                })
                .then(argument("confirm")
                        .executes(c -> {
                            CrownUser p = getUserSender(c);

                            if(p.isBaron()){
                                p.sendMessage("&7You are already a baron!");
                                return 0;
                            }

                            if(bals.getBalance(p.getBase()) < baronPrice) throw new CannotAffordTransactionException("You need at least 500,000 Rhines");

                            bals.setBalance(p.getBase(), bals.getBalance(p.getBase()) - baronPrice);
                            p.setBaron(true);
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Congratulations!&r You are now a &ebaron&r!"));
                            return 0;
                        })
                );
    }

    /*@Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) throws CrownException {
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
    }*/
}
