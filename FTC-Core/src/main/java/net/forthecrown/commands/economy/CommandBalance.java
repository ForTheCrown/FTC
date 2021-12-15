package net.forthecrown.commands.economy;

import net.forthecrown.core.Crown;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.Economy;
import net.forthecrown.user.CrownUser;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandBalance extends FtcCommand {

    public CommandBalance() {
        super("balance", Crown.inst());

        setAliases("bal", "cash", "money", "ebal", "ebalance", "emoney");
        setDescription("Shows a player's balance");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Describe the command
     *
     *
     * Valid usages of command:
     * - /<command> <args>
     *
     * Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> showBal(c.getSource(), getUserSender(c)))
                .then(argument("player", UserArgument.user()) //Player is checking someone else's balance
                        .executes(c -> showBal(c.getSource(), UserArgument.getUser(c, "player")))
                );
    }

    private int showBal(CommandSource sender, CrownUser user){
        Economy economy = Crown.getEconomy();
        boolean senderIsUser = user.getName().equals(sender.textName());
        Component formatted = FtcFormatter.rhines(economy.get(user.getUniqueId())).color(NamedTextColor.GOLD);

        Component text = senderIsUser ?
                Component.translatable("user.valueQuery.self", formatted) :
                Component.translatable("user.valueQuery.other", user.nickDisplayName().color(NamedTextColor.YELLOW), formatted);

        sender.sendMessage(text.color(NamedTextColor.GRAY));
        return 0;
    }
}
