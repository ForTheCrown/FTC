package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.FtcExceptionProvider;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.EnumChatFormat;

import java.util.Collection;

public class CommandPay extends CrownCommandBuilder {

    private final int maxMoneyAmount;

    public CommandPay(){
        super("pay", FtcCore.getInstance());

        maxMoneyAmount = FtcCore.getMaxMoneyAmount();

        setDescription("Pays another player money");
        register();
    }

    private final Balances bals = FtcCore.getBalances();

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
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("players", UserType.users())
                      .then(argument("amount", IntegerArgumentType.integer(1, maxMoneyAmount))
                              .suggests(suggestMonies())

                            .executes(c -> {
                                CrownUser user = getUserSender(c);
                                int amount = c.getArgument("amount", Integer.class);
                                Collection<CrownUser> users = UserType.getUsers(c, "players");

                                return pay(user, users, amount);
                            })
                      )
                );
    }

    private int pay(CrownUser user, Collection<CrownUser> targets, int amount) throws CommandSyntaxException {
        if(amount > bals.get(user.getUniqueId())) throw FtcExceptionProvider.CANNOT_AFFORD_TRANSACTION.create(Balances.getFormatted(amount));

        byte paidAmount = 0;

        for (CrownUser target: targets){
            if(user.equals(target)){
                if(targets.size() == 1) throw UserType.NO_USERS_FOUND.create();
                continue;
            }

            if(bals.get(user.getUniqueId()) < amount){
                user.sendMessage(new ChatComponentText("You cannot afford that").a(EnumChatFormat.GRAY));
                break;
            }

            bals.add(target.getUniqueId(), amount, false);
            bals.add(user.getUniqueId(), -amount, false);

            user.sendMessage(
                    Component.text("You've paid ")
                            .color(NamedTextColor.GRAY)
                            .append(Balances.formatted(amount).color(NamedTextColor.GOLD))
                            .append(Component.text(" to "))
                            .append(
                                    Component.text(target.getName())
                                            .color(NamedTextColor.YELLOW)
                                            .hoverEvent(target)
                                            .clickEvent(target.asClickEvent())
                            )
            );

            target.sendMessage(
                    Component.text("You've received ")
                            .color(NamedTextColor.GRAY)
                            .append(Balances.formatted(amount).color(NamedTextColor.GOLD))
                            .append(Component.text(" from "))
                            .append(
                                    Component.text(user.getName())
                                            .color(NamedTextColor.YELLOW)
                                            .hoverEvent(user)
                                            .clickEvent(user.asClickEvent())
                            )
            );
            paidAmount++;
        }

        if(paidAmount == 0) throw UserType.NO_USERS_FOUND.create();
        if(paidAmount > 1) user.sendMessage(
                Component.text("Paid " + paidAmount + " people. Lost ")
                        .color(NamedTextColor.GRAY)
                        .append(Balances.formatted(paidAmount * amount).color(NamedTextColor.YELLOW))
        );
        return 0;
    }
}
