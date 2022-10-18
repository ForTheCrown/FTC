package net.forthecrown.commands.economy;

import com.google.common.collect.Iterators;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.inventory.FtcItems;
import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

public class CommandDeposit extends FtcCommand {

    public CommandDeposit(){
        super("deposit");

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
        command
                .executes(c -> {
                    User user = getUserSender(c);
                    ItemStack held = user.getInventory().getItemInMainHand();

                    return depositCoins(user, Iterators.singletonIterator(held));
                })

                .then(literal("all")
                        .executes(c -> {
                            User user = getUserSender(c);
                            return depositCoins(user, ItemStacks.nonEmptyIterator(user.getInventory()));
                        })
                );
    }

    private int getSingleItemValue(ItemStack itemStack) {
        try {
            Component component = itemStack.getItemMeta().lore().get(0);
            String lore = Text.plain(component)
                    .replaceAll("[\\D]", "")
                    .trim();

            return Integer.parseInt(lore);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int depositCoins(User user, Iterator<ItemStack> it) throws CommandSyntaxException {
        int earned = 0;
        int coins = 0;

        while (it.hasNext()) {
            var item = it.next();

            if (ItemStacks.isEmpty(item)) {
                continue;
            }

            if (item.getType() != FtcItems.COIN_MATERIAL) {
                continue;
            }

            if (!item.hasItemMeta()
                    || !item.getItemMeta()
                    .lore()
                    .get(0)
                    .contains(Component.text("Worth "))
            ) {
                continue;
            }

            int coinValue = getSingleItemValue(item);

            if (coinValue == 0) {
                continue;
            }

            int itemQuantity = item.getAmount();
            earned += coinValue * itemQuantity;
            coins += itemQuantity;

            item.setAmount(0);
        }

        if (earned == 0) {
            throw Exceptions.HOLD_COINS;
        }

        user.addBalance(earned);
        user.sendMessage(Messages.deposit(coins, earned));
        return 0;
    }
}