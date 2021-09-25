package net.forthecrown.commands;

import net.forthecrown.commands.arguments.BaltopArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.BalanceMap;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnegative;

public class CommandBalanceTop extends FtcCommand {
    public CommandBalanceTop() {
        super("balancetop", Crown.inst());

        setAliases("baltop", "banktop", "cashtop", "topbals", "ebaltop", "ebalancetop");
        setDescription("Displays all the player's balances in order from biggest to smallest");
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
     * - /baltop
     * - /baltop <page number>
     *
     * Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> sendBaltopMessage(c.getSource().asBukkit(), 0))
                .then(argument("page", BaltopArgument.BALTOP)
                        .executes(c -> sendBaltopMessage(c.getSource().asBukkit(), c.getArgument("page", Integer.class)))
                );
    }

    //Send the message
    private int sendBaltopMessage(CommandSender sender, @Nonnegative int page){
        BalanceMap balMap = Crown.getEconomy().getMap();

        final TextComponent border = Component.text(" --------- ").style(Style.style(NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        TextComponent.Builder text = Component.text()
                .append(border)
                .append(Component.translatable("commands.baltop.header").color(NamedTextColor.YELLOW))
                .append(border)
                .append(Component.newline());

        if(page < 1) {
            text
                    .append(Component.translatable(
                            "commands.baltop.total",
                            NamedTextColor.YELLOW,
                            FtcFormatter.rhines((int) balMap.getTotalBalance()))
                    )
                    .append(Component.newline());
        }

        for(int i = 0 ; i < 10 ; i++) {
            if((page*10) + i >= balMap.size()) break;
            int index = (page*10) + i;

            Component entryText = balMap.getPrettyDisplay(index);
            if(entryText == null) continue;

            text
                    .append(Component.text((index+1) + ") ").color(NamedTextColor.GOLD))
                    .append(entryText)
                    .append(Component.newline());
        }

        Component pageAscending = page + 1 == BaltopArgument.MAX ? Component.space() : Component.text(" > ")
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/baltop " + (page + 2)))
                .hoverEvent(Component.translatable("spectatorMenu.next_page"));

        Component pageDescending = page == 0 ? Component.space() : Component.text(" < ")
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/baltop " + page))
                .hoverEvent(Component.translatable("spectatorMenu.previous_page"));

        Component footerMessage = Component.text()
                .color(NamedTextColor.YELLOW)
                .append(pageDescending)
                .append(Component.translatable("commands.baltop.footer", Component.text((page+1) + "/" + BaltopArgument.MAX)))
                .append(pageAscending)
                .build();

        text
                .append(border)
                .append(footerMessage)
                .append(border);

        sender.sendMessage(text.build());
        return 0;
    }
}
