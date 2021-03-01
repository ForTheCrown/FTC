package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

import java.util.UUID;

public class AddBalanceCommand extends CrownCommandBuilder {

    public AddBalanceCommand() {
        super("addbalance", FtcCore.getInstance());

        setAliases("addbal", "addcash", "addbank");
        setPermission("ftc.commands.addbalance");
        setUsage("&7Usage: &r/addbalance <player> <amount>");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Adds to a players balance
     *
     *
     * Valid usages of command:
     * - /addbalance <player> <amount>
     *
     * Permissions used:
     * - ftc.admin
     *
     * Referenced other classes:
     * - Balances
     * - FtcCore: FtcCore.getBalances
     *
     * Author: Wout
     */

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command
                .then(argument("player", StringArgumentType.word())
                        .suggests((commandContext, suggestionsBuilder) -> getPlayerList(suggestionsBuilder).buildFuture())

                        .then(argument("amount", IntegerArgumentType.integer())
                                .executes(context -> {
                                    String playerName = context.getArgument("player", String.class);
                                    UUID playerID = getUUID(playerName);
                                    Integer amount = context.getArgument("amount", Integer.class);
                                    Balances balances = FtcCore.getBalances();

                                    balances.addBalance(playerID, amount, false);
                                    context.getSource().getBukkitSender().sendMessage(playerName + " now has " + balances.getBalance(playerID) + " Rhines.");
                                    return 0;
                                })
                        )
                );
    }

    /*@Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(args.length != 2) return false;

        UUID targetUUID;
        try {
            targetUUID = FtcCore.getOffOnUUID(args[0]);
        } catch (Exception e){ throw new InvalidPlayerInArgument(sender, args[0]); }

        int amountToAdd;
        try {
            amountToAdd = Integer.parseInt(args[1]);
        } catch (Exception e){ throw new InvalidArgumentException(sender, args[1] + " &7must be a number"); }

        FtcCore.getBalances().addBalance(targetUUID, amountToAdd, false);
        sender.sendMessage(args[0] + " now has " + FtcCore.getBalances().getBalance(targetUUID) + " Rhines");
        return true;
    }*/
}
