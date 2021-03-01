package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

import java.util.UUID;

public class SetBalanceCommand extends CrownCommandBuilder {
    public SetBalanceCommand(){
        super("setbalance", FtcCore.getInstance());

        setAliases("setbal", "setcash", "setbank", "setmoney");
        setDescription("Sets a players balance.");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Sets a players balance
     *
     *
     * Valid usages of command:
     * - /setbalance <player> <amount>
     *
     * Permissions used:
     * - ftc.admin
     *
     * Referenced other classes:
     * - Balances
     * - Economy: Economy.getBalances
     *
     * Author: Botul
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

                                    balances.setBalance(playerID, amount);
                                    context.getSource().getBukkitSender().sendMessage(playerName + " now has " + balances.getBalance(playerID) + " Rhines.");
                                    return 0;
                                })
                        )
                );
    }

    /*@Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(args.length != 2) throw new TooLittleArgumentsException(sender);

        UUID targetUUID;
        try {
            targetUUID = FtcCore.getOffOnUUID(args[0]);
        } catch (NullPointerException e){ throw new InvalidPlayerInArgument(sender, args[0]); }

        int amountToSet;
        try {
            amountToSet = Integer.parseInt(args[1]);
        } catch (Exception e){ throw new InvalidArgumentException(sender, args[1] + " is not a number"); }

        FtcCore.getBalances().setBalance(targetUUID, amountToSet);
        sender.sendMessage(CrownUtils.translateHexCodes("&e" + args[0] + " &7now has &6" + amountToSet + " Rhines"));
        return true;
    }*/
}
