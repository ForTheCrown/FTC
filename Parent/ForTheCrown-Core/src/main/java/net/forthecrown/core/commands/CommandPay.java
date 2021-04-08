package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CannotAffordTransactionException;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandPay extends CrownCommandBuilder {

    private final int maxMoneyAmount;

    public CommandPay(){
        super("pay", FtcCore.getInstance());

        maxMoneyAmount = FtcCore.getMaxMoneyAmount();

        setDescription("Pays another player money");
        setUsage("&7Usage: &r/pay <user> <amount>");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Pays another player a set amount of money, removes the money from the player as well
     *
     *
     * Valid usages of command:
     * - /pay <player> <amount>
     *
     * Author: Botul
     */

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command
                .then(argument("player", UserType.user())
                        .suggests((c, b) -> UserType.listSuggestions(b))

                        .then(argument("amount", IntegerArgumentType.integer(1, maxMoneyAmount))
                                .executes(c ->{
                                    CrownUser user = getUserSender(c);
                                    Balances bals = FtcCore.getBalances();

                                    int amount = c.getArgument("amount", Integer.class);
                                    if(amount > bals.get(user.getUniqueId())) throw new CannotAffordTransactionException();

                                    CrownUser target = UserType.getUser(c, "player");

                                    bals.add(target.getUniqueId(), amount);
                                    bals.add(user.getUniqueId(), -amount);

                                    user.sendMessage(
                                            Component.text("You've paid ")
                                                    .color(NamedTextColor.GRAY)
                                                    .append(Balances.formatted(amount).color(NamedTextColor.GOLD))
                                                    .append(Component.text(" to "))
                                                    .append(Component.text(target.getName()).color(NamedTextColor.YELLOW).hoverEvent(target))
                                    );

                                    target.sendMessage(
                                            Component.text("You've received ")
                                                    .color(NamedTextColor.GRAY)
                                                    .append(Balances.formatted(amount).color(NamedTextColor.GOLD))
                                                    .append(Component.text(" from "))
                                                    .append(Component.text(user.getName()).color(NamedTextColor.YELLOW).hoverEvent(user))
                                    );

                                    return 0;
                                })
                        )
                );
    }
}
