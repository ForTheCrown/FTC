package net.forthecrown.commands;

import net.forthecrown.core.Crown;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.Economy;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandDeposit extends FtcCommand {

    public CommandDeposit(){
        super("deposit", Crown.inst());

        setDescription("Allows you to deposit coins into your balance");

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
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            Player player = getPlayerSender(c);

            if(player.getInventory().getItemInMainHand().getType() != Material.SUNFLOWER
                    && (!player.getInventory().getItemInMainHand().hasItemMeta()
                    || !player.getInventory().getItemInMainHand().getItemMeta().lore().get(0).contains(Component.text("Worth "))))
                throw FtcExceptionProvider.holdingCoins();

            ItemStack mainItem = player.getInventory().getItemInMainHand();

            int amount;
            try {
                Component component = mainItem.getItemMeta().lore().get(0);
                String lore = ChatColor.stripColor(ChatUtils.getString(component)).replaceAll("[\\D]", "").trim();
                amount = Integer.parseInt(lore);
            } catch (NumberFormatException e) { throw FtcExceptionProvider.holdingCoins(); }

            amount = amount*mainItem.getAmount();

            Economy bals = Crown.getEconomy();
            bals.add(player.getUniqueId(), amount, false);

            player.sendMessage(
                    Component.text()
                            .color(NamedTextColor.GRAY)
                            .append(
                                    Component.translatable("commands.deposited",
                                            Component.text(mainItem.getAmount() + " coin" + FtcUtils.addAnS(mainItem.getAmount())).color(NamedTextColor.YELLOW),
                                            FtcFormatter.rhines(amount).color(NamedTextColor.GOLD)
                                    )
                            )
            );
            player.getInventory().removeItem(mainItem);
            return 0;
        });
    }
}
