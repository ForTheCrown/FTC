package net.forthecrown.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandBalance extends CrownCommandBuilder {

    public CommandBalance() {
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
                    player.sendMessage(ChatColor.GOLD + "$" + ChatColor.GRAY + " You currently have " + ChatColor.GOLD + balances.getDecimalized(player.getUniqueId()) +" Rhines");
                    return 0;
                })
                .then(argument("player", UserType.user())
                        .suggests((c, s) -> UserType.listSuggestions(s))

                        .executes(c ->{
                            CommandSender sender = c.getSource().getBukkitSender();
                            CrownUser target = UserType.getUser(c, "player");

                            sender.sendMessage(ChatColor.GOLD + "$ " + ChatColor.YELLOW + target.getName() + ChatColor.GRAY + " currently has " + ChatColor.GOLD + balances.getDecimalized(target.getBase()) + " Rhines");
                            return 0;
                        })
                );
    }
}
