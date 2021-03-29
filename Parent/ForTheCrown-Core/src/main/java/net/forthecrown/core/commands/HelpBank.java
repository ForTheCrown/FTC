package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.utils.CrownItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class HelpBank extends CrownCommandBuilder {

    public HelpBank(){
        super("bank", FtcCore.getInstance());

        setAliases("bankhelp", "helpbank");
        setPermission(null);
        register();
    }

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUser u = getUserSender(c);

            Component component = Component.text()
                    .append(FtcCore.prefix())
                    .append(Component.text("Bank info:").color(NamedTextColor.YELLOW))
                    .append(Component.newline())

                    .append(Component.text("The Bank ").color(NamedTextColor.YELLOW))
                    .append(Component.text("can provide you with extra items in your adventure on "))
                    .append(Component.text("FTC").color(NamedTextColor.GOLD)).append(Component.text("."))
                    .append(Component.newline())
                    .append(Component.text("To enter the bank, you need a "))
                    .append(Component.text("[Bank Ticket]")
                            .hoverEvent(CrownItems.VOTE_TICKET.asHoverEvent())
                            .color(NamedTextColor.AQUA))
                    .append(Component.text(" earned by voting for the server with"))
                    .append(Component.text(" /vote.")
                            .color(NamedTextColor.GOLD)
                            .hoverEvent(HoverEvent.showText(Component.text("Click here to vote for the server")))
                            .clickEvent(ClickEvent.runCommand("/vote")))
                    .append(Component.newline())
                    .append(Component.text("Entering "))
                    .append(Component.text("the vault ").color(NamedTextColor.YELLOW))
                    .append(Component.text("will consume the ticket, allowing you to "))
                    .append(Component.text("loot the chests ").color(NamedTextColor.YELLOW))
                    .append(Component.text("for a short period of time."))

                    .build();

            u.sendMessage(component);
            return 0;
        });
    }
}
