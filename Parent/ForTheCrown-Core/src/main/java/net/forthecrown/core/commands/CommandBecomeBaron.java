package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CannotAffordTransactionException;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.utils.CrownUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;

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
    protected void registerCommand(BrigadierCommand command) {
        int baronPrice = FtcCore.getInstance().getConfig().getInt("BaronPrice");
        Balances bals = FtcCore.getBalances();

        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);

                    if(user.isBaron()){
                        user.sendMessage("&7You are already a baron!");
                        return 0;
                    }

                    if(bals.get(user.getUniqueId()) < baronPrice) throw new CannotAffordTransactionException("You need at least " + CrownUtils.decimalizeNumber(baronPrice) + " Rhines");

                    TextComponent message = Component.text()
                            .append(FtcCore.prefix())
                            .append(Component.text("Are you sure you wish to become a "))
                            .append(Rank.BARON.noEndSpacePrefix())
                            .append(Component.text("? This will cost "))
                            .append(Component.text(CrownUtils.decimalizeNumber(baronPrice) + " Rhines ").color(NamedTextColor.YELLOW))

                            .append(Component.text("[Confirm]")
                                    .color(NamedTextColor.GREEN)
                                    .clickEvent(ClickEvent.runCommand("/" + getName() + " confirm"))
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to become a baron")))
                            )

                            .build();

                    user.sendMessage(message);
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
