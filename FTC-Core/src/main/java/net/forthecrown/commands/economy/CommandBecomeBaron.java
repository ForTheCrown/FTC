package net.forthecrown.commands.economy;

import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.Economy;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.Rank;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.data.RankTitle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandBecomeBaron extends FtcCommand {
    public CommandBecomeBaron() {
        super("becomebaron", Crown.inst());

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
     * Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        Economy bals = Crown.getEconomy();

        command
                .executes(c -> {
                    CrownUser p = getUserSender(c);
                    int baronPrice = ComVars.getBaronPrice();

                    if(p.hasTitle(RankTitle.BARON)) throw FtcExceptionProvider.alreadyBaron();
                    if(!bals.has(p.getUniqueId(), baronPrice)) throw FtcExceptionProvider.cannotAfford(baronPrice);

                    //Tell em cost and ask for confirmation
                    TextComponent message = Component.text()
                            .append(Crown.prefix())
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

                            if(p.hasTitle(RankTitle.BARON)) throw FtcExceptionProvider.alreadyBaron();
                            if(!bals.has(p.getUniqueId(), baronPrice)) throw FtcExceptionProvider.cannotAfford(baronPrice);

                            bals.add(p.getUniqueId(), -baronPrice);

                            p.addTitle(RankTitle.BARON);
                            p.addTitle(RankTitle.BARONESS);

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
