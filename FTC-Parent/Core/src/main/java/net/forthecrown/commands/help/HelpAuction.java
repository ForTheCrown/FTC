package net.forthecrown.commands.help;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class HelpAuction extends FtcCommand {

    public HelpAuction() {
        super("auctionhelp", ForTheCrown.inst());

        setAliases("helpauction", "help_auction", "auction_help");
        setPermission(Permissions.HELP);

        register();
    }

    private final Component message = makeMessage();

    private Component makeMessage(){
        return Component.text()
                .append(ForTheCrown.prefix())
                .append(Component.text("Auction info:").color(NamedTextColor.YELLOW))

                .append(Component.newline())



                .build();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /<command>
     *
     * Permissions used:
     * See HELP constant in Permissions
     *
     * Main Author: Botul
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    c.getSource().sendMessage(message);
                    return 0;
                });
    }
}