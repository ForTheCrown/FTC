package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandBalance extends CrownCommandBuilder {

    public CommandBalance() {
        super("balance", FtcCore.getInstance());

        setUsage("&7Usage: &r/balance <player>");
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
    protected void registerCommand(BrigadierCommand command) {
        Balances balances = FtcCore.getBalances();

        command
                .executes(c -> {
                    Player player = getPlayerSender(c);
                    player.sendMessage(ChatColor.GOLD + "$" + ChatColor.GRAY + " You currently have " + ChatColor.GOLD + balances.getWithCurrency(player.getUniqueId()));
                    return 0;
                })
                .then(argument("player", UserType.user())
                        .suggests((c, s) -> UserType.listSuggestions(s))

                        .executes(c ->{
                            CommandSender sender = c.getSource().getBukkitSender();
                            CrownUser target = UserType.getUser(c, "player");

                            Component text = Component.text()
                                    .color(NamedTextColor.GRAY)
                                    .append(Component.text("$ ").color(NamedTextColor.GOLD))
                                    .append(target.name()
                                            .color(NamedTextColor.YELLOW)
                                            .hoverEvent(target.asHoverEvent())
                                            .clickEvent(ClickEvent.suggestCommand("/w " + target.getName()))
                                    )
                                    .append(Component.text(" currently has "))
                                    .append(Component.text(balances.getWithCurrency(target.getUniqueId())).color(NamedTextColor.GOLD))
                                    .build();

                            sender.sendMessage(text);
                            return 0;
                        })
                );
    }
}
