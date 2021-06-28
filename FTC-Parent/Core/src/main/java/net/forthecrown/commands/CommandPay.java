package net.forthecrown.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.commands.arguments.UserType;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.economy.Balances;
import net.forthecrown.user.CrownUser;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.royalgrenadier.types.selector.EntityArgumentImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.UUID;

public class CommandPay extends FtcCommand {

    private final int maxMoneyAmount;

    public CommandPay(){
        super("pay", CrownCore.inst());

        maxMoneyAmount = CrownCore.getMaxMoneyAmount();

        setDescription("Pays another player");
        register();
    }

    private final Balances bals = CrownCore.getBalances();

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
                                List<CrownUser> users = UserType.getUsers(c, "players");

                                return pay(user, users, amount, null);
                            })

                              .then(argument("message", StringArgumentType.greedyString())
                                      .executes(c -> {
                                          CrownUser user = getUserSender(c);
                                          int amount = c.getArgument("amount", Integer.class);
                                          List<CrownUser> users = UserType.getUsers(c, "players");
                                          Component message = ChatFormatter.formatStringIfAllowed(c.getArgument("message", String.class), user);

                                          return pay(user, users, amount, message);
                                      })
                              )
                      )
                );
    }

    private int pay(CrownUser user, List<CrownUser> targets, int amount, Component message) throws CommandSyntaxException {
        if(!user.allowsPaying()) throw FtcExceptionProvider.senderPayDisabled();

        UUID id = user.getUniqueId();
        if(!bals.canAfford(id, amount)) throw FtcExceptionProvider.cannotAfford(amount);

        if(targets.remove(user) && targets.isEmpty()) throw FtcExceptionProvider.cannotPaySelf();
        if(targets.isEmpty()) throw EntityArgumentImpl.NO_ENTITIES_FOUND.create();
        if(!bals.canAfford(user.getUniqueId(), amount * targets.size())) throw FtcExceptionProvider.cannotAfford();

        byte paidAmount = 0;

        if(targets.size() == 1){
            CrownUser target = targets.get(0);
            if(!target.allowsPaying()) throw FtcExceptionProvider.targetPayDisabled(target);
        }

        for (CrownUser target: targets){
            bals.add(target.getUniqueId(), amount, false);
            bals.add(id, -amount, false);

            user.sendMessage(
                    Component.text("You've paid ")
                            .color(NamedTextColor.GRAY)
                            .append(Balances.formatted(amount).color(NamedTextColor.GOLD))
                            .append(Component.text(" to "))
                            .append(target.nickDisplayName().color(NamedTextColor.YELLOW))
                            .append(message == null ? Component.empty() : Component.text(": ").append(message.color(NamedTextColor.WHITE)))
            );

            target.sendMessage(
                    Component.text("You've received ")
                            .color(NamedTextColor.GRAY)
                            .append(Balances.formatted(amount).color(NamedTextColor.GOLD))
                            .append(Component.text(" from "))
                            .append(user.nickDisplayName().color(NamedTextColor.YELLOW))
                            .append(message == null ? Component.empty() : Component.text(": ").append(message.color(NamedTextColor.WHITE)))
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
