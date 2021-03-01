package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BalanceCommand extends CrownCommandBuilder {

    public BalanceCommand() {
        super("balance", FtcCore.getInstance());

        setUsage("&7Usage: &r/balance <player>");
        setAliases("bal", "bank", "cash", "money");
        setDescription("Displays a player's balance");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Displays either the executors balance or another players balance
     *
     *
     * Valid usages of command:
     * - /balance
     * - /balance <player>
     *
     * Referenced other classes:
     * - Balances:
     * - Economy: Economy.getBalances
     * - FtcCore: FtcCore.getOnOffUUID
     *
     * Author: Botul
     */

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        Balances balances = FtcCore.getBalances();

        command
                .executes(c -> {
                    Player player = getPlayerSender(c);
                    player.sendMessage(ChatColor.GOLD + "$" + ChatColor.GRAY + " You currently have " + ChatColor.GOLD + balances.getDecimalizedBalance(player.getUniqueId()) +" Rhines");
                    return 0;
                })
                .then(argument("player", StringArgumentType.word())
                        .suggests((c, s) -> getPlayerList(s).buildFuture())

                        .executes(c ->{
                            CommandSender sender = c.getSource().getBukkitSender();

                            String playerName = c.getArgument("player", String.class);
                            UUID target = getUUID(playerName);

                            sender.sendMessage(ChatColor.GOLD + "$ " + ChatColor.YELLOW + playerName + ChatColor.GRAY + " currently has " + ChatColor.GOLD + balances.getDecimalizedBalance(target) + " Rhines");
                            return 0;
                        })
                );
    }

/*
    @Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) throws CrownException {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);

        Player player = (Player) sender;
        Balances bals = FtcCore.getBalances();

        if(args.length < 1){
            player.sendMessage(ChatColor.GOLD + "$" + ChatColor.GRAY + " You currently have " + ChatColor.GOLD + bals.getDecimalizedBalance(player.getUniqueId()) +" Rhines");
            return true;
        }

        UUID targetUUID = FtcCore.getOffOnUUID(args[0]);
        if(targetUUID == null) throw new InvalidPlayerInArgument(sender, args[0]);

        player.sendMessage(ChatColor.GOLD + "$ " + ChatColor.YELLOW + args[0] + ChatColor.GRAY + " currently has " + ChatColor.GOLD + bals.getDecimalizedBalance(targetUUID) + " Rhines");
        return true;
    }*/
}
