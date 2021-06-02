package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.arguments.BaltopType;
import net.forthecrown.emperor.economy.BalanceMap;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnegative;

public class CommandBalanceTop extends CrownCommandBuilder {
    public CommandBalanceTop() {
        super("balancetop", CrownCore.inst());

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
     * Author: Botul
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> { //No args -> show first page
                    sendBaltopMessage(c.getSource().asBukkit(), 0);
                    return 0;
                })
                .then(argument("page", BaltopType.BALTOP)
                        .executes(c -> { //Page number given -> show that page
                            Integer soup = c.getArgument("page", Integer.class); //Delicious soup
                            sendBaltopMessage(c.getSource().asBukkit(), soup);
                            return 0;
                        })
                );
    }

    //Send the message
    private void sendBaltopMessage(CommandSender sender, @Nonnegative int page){
        BalanceMap balMap = CrownCore.getBalances().getMap();

        final TextComponent border = Component.text(" --------- ").style(Style.style(NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        TextComponent.Builder text = Component.text()
                .append(border)
                .append(Component.translatable("economy.baltop.header").color(NamedTextColor.YELLOW))
                .append(border)
                .append(Component.newline());

        for(int i = 0 ; i < 10 ; i++){
            if((page*10) + i >= balMap.size()) break;
            int index = (page*10) + i;

            Component entryText = balMap.getPrettyDisplay(index);
            if(entryText == null) continue;

            text.append(Component.text((index+1) + ") ").color(NamedTextColor.GOLD))
                    .append(entryText)
                    .append(Component.newline());
        }

        Component pageAscending = page + 1 == BaltopType.MAX ? Component.empty() : Component.text("> ")
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/baltop " + (page + 2)))
                .hoverEvent(Component.text("Next page"));

        Component pageDescending = page == 0 ? Component.empty() : Component.text(" <")
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/baltop " + page))
                .hoverEvent(Component.text("Last page"));

        Component footerMessage = Component.text()
                .color(NamedTextColor.YELLOW)
                .append(pageDescending)
                .append(Component.space())
                .append(Component.translatable("economy.baltop.footer", Component.text((page+1) + "/" + BaltopType.MAX)))
                .append(Component.space())
                .append(pageAscending)
                .build();

        text
                .append(border)
                .append(footerMessage)
                .append(border);

        // ngl, now that this is just sending one message that's appended together, there's no weird 1 frame thing where
        // the text gets sent line by line lol. It just comes out as one :D
        sender.sendMessage(text.build());
    }
}
