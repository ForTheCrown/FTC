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

public class CommandBecomeBaron extends CrownCommandBuilder {
    public CommandBecomeBaron() {
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
     * - ftc.commands.becomebaron
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

                    if(bals.get(user.getBase()) < baronPrice) throw new CannotAffordTransactionException("You need at least " + decimalFormat.format(baronPrice) + " Rhines");

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

                            if(bals.get(p.getBase()) < baronPrice) throw new CannotAffordTransactionException("You need at least 500,000 Rhines");

                            bals.set(p.getBase(), bals.get(p.getBase()) - baronPrice);
                            p.setBaron(true);
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Congratulations!&r You are now a &ebaron&r!"));
                            return 0;
                        })
                );
    }
}
