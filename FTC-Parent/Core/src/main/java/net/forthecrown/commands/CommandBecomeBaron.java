package net.forthecrown.commands;

import net.forthecrown.core.ComVars;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.Balances;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.enums.Rank;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandBecomeBaron extends FtcCommand {
    public CommandBecomeBaron() {
        super("becomebaron", ForTheCrown.inst());

        setPermission(Permissions.BECOME_BARON);
        setDescription("Allows you to become a baron");

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
    protected void createCommand(BrigadierCommand command) {
        Balances bals = ForTheCrown.getBalances();

        command
                .executes(c -> {
                    CrownUser p = getUserSender(c);
                    int baronPrice = ComVars.getBaronPrice();

                    if(p.hasRank(Rank.BARON)) throw FtcExceptionProvider.alreadyBaron();
                    if(!bals.canAfford(p.getUniqueId(), baronPrice)) throw FtcExceptionProvider.cannotAfford(baronPrice);

                    //Tell em cost and ask for confirmation
                    TextComponent message = Component.text()
                            .append(ForTheCrown.prefix())
                            .append(Component.translatable("commands.becomeBaron.confirm", Rank.BARON.noEndSpacePrefix(), FtcFormatter.rhines(baronPrice).color(NamedTextColor.YELLOW)))
                            .append(Component.space())
                            .append(Component.translatable("buttons.confirm")
                                    .color(NamedTextColor.GREEN)
                                    .clickEvent(ClickEvent.runCommand("/" + getName() + " confirm"))
                                    .hoverEvent(HoverEvent.showText(Component.translatable("commands.becomeBaron.confirm.hover")))
                            )

                            .build();

                    //send message
                    p.sendMessage(message);
                    return 0;
                })
                .then(literal("confirm")
                        .executes(c -> {
                            CrownUser p = getUserSender(c);
                            int baronPrice = ComVars.getBaronPrice();

                            if(p.hasRank(Rank.BARON)) throw FtcExceptionProvider.alreadyBaron();
                            if(!bals.canAfford(p.getUniqueId(), baronPrice)) throw FtcExceptionProvider.cannotAfford(baronPrice);

                            bals.add(p.getUniqueId(), -baronPrice);

                            p.addRank(Rank.BARON);
                            p.addRank(Rank.BARONESS);
                            p.setBaron(true);

                            Component text = Component.text()
                                    .append(Component.translatable("commands.becomeBaron.congrats").color(NamedTextColor.GOLD))
                                    .append(Component.space())
                                    .append(Component.translatable("commands.becomeBaron.congrats2", Component.text("baron").color(NamedTextColor.YELLOW)))
                                    .build();

                            p.sendMessage(text);
                            return 0;
                        })
                );
    }
}
