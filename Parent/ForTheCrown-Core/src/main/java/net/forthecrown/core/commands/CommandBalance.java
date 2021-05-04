package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandBalance extends CrownCommandBuilder {

    public CommandBalance() {
        super("balance", FtcCore.getInstance());

        setAliases("bal", "cash", "money", "ebal", "ebalance", "emoney");
        setDescription("Displays a player's balance");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Describe the command
     *
     *
     * Valid usages of command:
     * - /<command> <args>
     *
     * Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> { //No args, player is checking their own balance
                    Player player = getPlayerSender(c);
                    player.sendMessage(ChatColor.GOLD + "$" + ChatColor.GRAY + " You currently have " + ChatColor.GOLD + FtcCore.getBalances().getWithCurrency(player.getUniqueId()));
                    return 0;
                })
                .then(argument("player", UserType.user()) //Player is checking someone else's balance

                        .executes(c ->{
                            CommandSender sender = c.getSource().asBukkit();
                            CrownUser target = UserType.getUser(c, "player");
                            Balances balances = FtcCore.getBalances();

                            //Make the message
                            Component text = Component.text()
                                    .color(NamedTextColor.GRAY)
                                    .append(Component.text("$ ").color(NamedTextColor.GOLD))
                                    .append(target.name()
                                            .color(NamedTextColor.YELLOW)
                                            .hoverEvent(target)
                                            .clickEvent(target.asClickEvent())
                                    )
                                    .append(Component.text(" currently has "))
                                    .append(balances.withCurrency(target.getUniqueId()).color(NamedTextColor.GOLD))
                                    .build();

                            //send the message
                            sender.sendMessage(text);
                            return 0;
                        })
                );
    }
}
