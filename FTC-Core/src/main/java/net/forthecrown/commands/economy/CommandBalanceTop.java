package net.forthecrown.commands.economy;

import net.forthecrown.commands.arguments.BaltopArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.BalanceMap;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.Validate;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnegative;

public class CommandBalanceTop extends FtcCommand {
    public static final int ENTRIES_PER_PAGE = 10;

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

        final TextComponent border = Component.text("          ").style(Style.style(NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        TextComponent.Builder text = Component.text()
                .append(border)
                .append(Component.space())
                .append(Component.translatable("commands.baltop.header").color(NamedTextColor.YELLOW))
                .append(Component.space())
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

        int firstIndex = ENTRIES_PER_PAGE * page;
        for(int i = 0 ; i < ENTRIES_PER_PAGE ; i++) {
            int index = firstIndex + i;
            if(index >= balMap.size()) break;

            Component entryText = getPrettyDisplay(balMap, index);
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

    public static Component getPrettyDisplay(BalanceMap balMap, int index) {
        Validate.isTrue(index >= 0 && index < balMap.size(), "Invalid index: " + index + " from size " + balMap.size());

        BalanceMap.Balance entry = balMap.getEntry(index);
        if (!UserManager.isPlayerID(entry.getUniqueId())) return null;

        CrownUser user = UserManager.getUser(entry.getUniqueId());
        Component displayName = user.nickDisplayName();

        user.unloadIfOffline();

        return Component.text()
                .append(displayName)
                .append(Component.text(" - "))
                .append(FtcFormatter.rhines(entry.getValue()).color(NamedTextColor.YELLOW))
                .build();
    }
}