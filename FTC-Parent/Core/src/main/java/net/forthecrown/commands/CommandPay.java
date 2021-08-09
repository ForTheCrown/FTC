package net.forthecrown.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Permissions;
import net.forthecrown.economy.Balances;
import net.forthecrown.user.CrownUser;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.royalgrenadier.types.selector.EntityArgumentImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.UUID;

public class CommandPay extends FtcCommand {

    private final int maxMoneyAmount;

    public CommandPay(){
        super("pay", ForTheCrown.inst());

        maxMoneyAmount = ComVars.getMaxMoneyAmount();

        setDescription("Pays another player");
        setPermission(Permissions.PAY);

        register();
    }

    private final Balances bals = ForTheCrown.getBalances();

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Pays another player a set amount of money, removes the money from the player as well
     *
     * Valid usages of command:
     * - /pay <player> <amount> [message]
     *
     * Author: Botul
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("players", UserArgument.users())
                      .then(argument("amount", IntegerArgumentType.integer(1, maxMoneyAmount))
                              .suggests(suggestMonies())

                            .executes(c -> {
                                CrownUser user = getUserSender(c);
                                int amount = c.getArgument("amount", Integer.class);
                                List<CrownUser> users = UserArgument.getUsers(c, "players");

                                return pay(user, users, amount, null);
                            })

                              .then(argument("message", StringArgumentType.greedyString())
                                      .executes(c -> {
                                          CrownUser user = getUserSender(c);
                                          int amount = c.getArgument("amount", Integer.class);
                                          List<CrownUser> users = UserArgument.getUsers(c, "players");
                                          Component message = FtcFormatter.formatIfAllowed(c.getArgument("message", String.class), user);

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

        targets.removeIf(u -> !u.allowsPaying());

        if(targets.remove(user) && targets.isEmpty()) throw FtcExceptionProvider.cannotPaySelf();
        if(targets.isEmpty()) throw EntityArgumentImpl.NO_ENTITIES_FOUND.create();
        if(!bals.canAfford(user.getUniqueId(), amount * targets.size())) throw FtcExceptionProvider.cannotAfford();

        byte paidAmount = 0;

        if(targets.size() == 1){
            CrownUser target = targets.get(0);
            if(!target.allowsPaying()) throw FtcExceptionProvider.targetPayDisabled(target);
            if(target.getInteractions().isBlockedPlayer(user.getUniqueId())) throw FtcExceptionProvider.cannotPayBlocked();
        }

        Component messageActual = message == null || !user.getInteractions().muteStatus().maySpeak ?
                Component.text(".").color(NamedTextColor.GRAY) :
                Component.text(": ").append(message.color(NamedTextColor.WHITE));

        Component formattedAmount = FtcFormatter.rhines(amount).color(NamedTextColor.GOLD);

        for (CrownUser target: targets) {
            bals.add(target.getUniqueId(), amount, false);
            bals.add(id, -amount, false);

            user.sendMessage(
                    Component.translatable("economy.pay.sender",
                            NamedTextColor.GRAY,
                            formattedAmount,
                            target.nickDisplayName().color(NamedTextColor.YELLOW),
                            message == null ? messageActual : message.color(NamedTextColor.WHITE)
                    )
            );

            boolean ignoring = target.getInteractions().isBlockedPlayer(user.getUniqueId());
            target.sendMessage(
                    Component.translatable("economy.pay.receiver",
                            NamedTextColor.GRAY,
                            formattedAmount,
                            user.nickDisplayName().color(NamedTextColor.YELLOW),
                            ignoring ? Component.text(".").color(NamedTextColor.GRAY) : messageActual
                    )
            );

            paidAmount++;
        }

        if(paidAmount == 0) throw UserArgument.NO_USERS_FOUND.create();

        if(paidAmount > 1) {
            user.sendMessage(
                    Component.translatable("economy.pay.result",
                            NamedTextColor.GRAY,
                            Component.text(paidAmount).color(NamedTextColor.YELLOW),
                            FtcFormatter.rhines(paidAmount * amount).color(NamedTextColor.GOLD)
                    )
            );
        }
        return 0;
    }
}
