package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.Bukkit;

import java.util.UUID;

public class PayCommand extends CrownCommandBuilder {

    public PayCommand(){
        super("pay", FtcCore.getInstance());

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
     * Referenced other classes:
     * - Balances:
     * - Economy: Economy.getBalances
     *
     * Author: Botul
     */

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command
                .then(argument("player", StringArgumentType.word())
                        .suggests((c, b) -> getPlayerList(b).buildFuture())

                        .then(argument("amount", IntegerArgumentType.integer(1, FtcCore.getMaxMoneyAmount()))
                                .executes(c ->{
                                    CrownUser user = getUserSender(c);
                                    Balances bals = FtcCore.getBalances();

                                    int amount = c.getArgument("amount", Integer.class);
                                    if(amount > bals.getBalance(user.getBase())) throw new CrownCommandException("You cannot afford to pay " + amount);

                                    String targetName = c.getArgument("player", String.class);
                                    UUID id = getUUID(targetName);

                                    bals.addBalance(id, amount);
                                    bals.addBalance(user.getBase(), -amount);

                                    user.sendMessage(ChatColor.GRAY + "You've paid " + ChatColor.GOLD + amount + " Rhines " + ChatColor.GRAY + "to " + ChatColor.YELLOW + targetName);
                                    try{
                                        Bukkit.getPlayer(id).sendMessage(ChatColor.GRAY + "You've received " + ChatColor.GOLD + amount + " Rhines " + ChatColor.GRAY + "by " + ChatColor.YELLOW + user.getName());
                                    } catch (Exception ignored){}
                                    return 0;
                                })
                        )
                );
    }

    /*@Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);
        if(args.length != 2) throw new TooLittleArgumentsException(sender);

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        UUID target;

        if(args[0].contains(player.getName())) throw new InvalidArgumentException(sender, "You can't pay yourself");

        try {
            target = FtcCore.getOffOnUUID(args[0]);
        } catch (Exception e){ throw new InvalidPlayerInArgument(sender, args[0]); }

        int amountToPay;
        try {
            amountToPay = Integer.parseInt(args[1]);
        } catch (Exception e){ throw new InvalidArgumentException(sender, "The amount to pay must be a number!"); }

        if(amountToPay <= 0) throw new InvalidArgumentException(sender, "You can't pay negative amounts");

        Balances bals = FtcCore.getBalances();

        if(bals.getBalance(playerUUID) < amountToPay) throw new CannotAffordTransaction(sender);

        //gives money to target
        bals.addBalance(target, amountToPay);

        //removes money from player
        bals.addBalance(playerUUID, -amountToPay);

        player.sendMessage(ChatColor.GRAY + "You've paid " + ChatColor.GOLD + amountToPay + " Rhines " + ChatColor.GRAY + "to " + ChatColor.YELLOW + args[0]);
        try{
            Bukkit.getPlayer(target).sendMessage(ChatColor.GRAY + "You've received " + ChatColor.GOLD + amountToPay + " Rhines " + ChatColor.GRAY + "by " + ChatColor.YELLOW + player.getName());
        } catch (Exception e){
            return true;
        }

        return true;
    }*/
}
