package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CannotAffordTransactionException;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.Bukkit;

import java.util.UUID;

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
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command
                .then(argument("player", StringArgumentType.word())
                        .suggests((c, b) -> UserType.listSuggestions(b))

                        .then(argument("amount", IntegerArgumentType.integer(1, maxMoneyAmount))
                                .executes(c ->{
                                    CrownUser user = getUserSender(c);
                                    Balances bals = FtcCore.getBalances();

                                    int amount = c.getArgument("amount", Integer.class);
                                    if(amount > bals.get(user.getBase())) throw new CannotAffordTransactionException();

                                    String targetName = c.getArgument("player", String.class);
                                    UUID id = getUUID(targetName);

                                    bals.add(id, amount);
                                    bals.add(user.getBase(), -amount);

                                    user.sendMessage(ChatColor.GRAY + "You've paid " + ChatColor.GOLD + CrownUtils.decimalizeNumber(amount) + " Rhines " + ChatColor.GRAY + "to " + ChatColor.YELLOW + targetName);
                                    try{
                                        Bukkit.getPlayer(id).sendMessage(ChatColor.GRAY + "You've received " + ChatColor.GOLD + CrownUtils.decimalizeNumber(amount) + " Rhines " + ChatColor.GRAY + "from " + ChatColor.YELLOW + user.getName());
                                    } catch (Exception ignored){}
                                    return 0;
                                })
                        )
                );
    }
}
