package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.economy.Balances;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandBalance extends FtcCommand {

    public CommandBalance() {
        super("balance", CrownCore.inst());

        setAliases("bal", "cash", "money", "ebal", "ebalance", "emoney");
        setDescription("Displays a player's balance");

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
     * Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> showBal(c.getSource(), getUserSender(c)))
                .then(argument("player", UserType.user()) //Player is checking someone else's balance
                        .executes(c -> showBal(c.getSource(), UserType.getUser(c, "player")))
                );
    }

    private int showBal(CommandSource sender, CrownUser user){
        Balances balances = CrownCore.getBalances();
        boolean senderIsUser = user.getName().equals(sender.textName());
        Component formatted = Balances.formatted(balances.get(user.getUniqueId())).color(NamedTextColor.GOLD);

        Component text = senderIsUser ?
                Component.translatable("user.valueQuery.self", formatted) :
                Component.translatable("user.valueQuery.other", user.nickDisplayName().color(NamedTextColor.YELLOW), formatted);

        sender.sendMessage(text.color(NamedTextColor.GRAY));
        return 0;
    }
}
