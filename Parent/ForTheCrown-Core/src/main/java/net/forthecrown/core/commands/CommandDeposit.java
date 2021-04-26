package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.forthecrown.core.utils.ComponentUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandDeposit extends CrownCommandBuilder {

    public CommandDeposit(){
        super("deposit", FtcCore.getInstance());

        setUsage("&7Usage: &r/deposit");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Adds all the coins in a person's hand to their balance
     *
     * Valid usages of command:
     * - /deposit
     *
     * Main Author: Botul
     */

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command.executes(c -> {
            Player player = getPlayerSender(c);

            if(player.getInventory().getItemInMainHand().getType() != Material.SUNFLOWER
                    && (!player.getInventory().getItemInMainHand().hasItemMeta()
                    || !player.getInventory().getItemInMainHand().getItemMeta().lore().get(0).contains(Component.text("Worth "))))
                throw new CrownCommandException("You need to be holding the coins you wish to deposit");

            ItemStack mainItem = player.getInventory().getItemInMainHand();

            int amount;
            try {
                Component component = mainItem.getItemMeta().lore().get(0);
                String lore = ChatColor.stripColor(ComponentUtils.getString(component)).replaceAll("[\\D]", "").trim();
                amount = Integer.parseInt(lore);
            } catch (NumberFormatException e) { throw new CrownCommandException("You need to be holding the coins you wish to deposit"); }

            amount = amount*mainItem.getAmount();

            Balances bals = FtcCore.getBalances();
            bals.add(player.getUniqueId(), amount, false);

            player.sendMessage(
                    Component.text()
                            .color(NamedTextColor.GRAY)
                            .append(Component.text("You deposited " + mainItem.getAmount() + " coin" +
                                    (mainItem.getAmount() == 1 ? "" : "s") +
                                    " and received "
                            ))
                            .append(Balances.formatted(amount).color(NamedTextColor.GOLD))
            );
            player.getInventory().removeItem(mainItem);
            return 0;
        });
    }
}
