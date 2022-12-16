package net.forthecrown.commands.economy;

import com.google.common.collect.Iterators;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.economy.TransactionType;
import net.forthecrown.economy.Transactions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.inventory.FtcItems;
import net.forthecrown.core.Messages;
import net.forthecrown.utils.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.regex.Pattern;

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

    private static final Pattern WORTH_PATTERN = Pattern.compile("Worth [\\d,.]+ Rhine(s|)");

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
            Component component = itemStack.getItemMeta()
                    .lore()
                    .get(0);

            String lore = Text.plain(component)
                    .replaceAll("\\D+", "")
                    .trim();

            return Integer.parseInt(lore);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int depositCoins(User user, Iterator<ItemStack> it) throws CommandSyntaxException {
        int earned = 0;
        int coins = 0;

        while (it.hasNext()) {
            var item = it.next();

            if (!isCoin(item)) {
                continue;
            }

            int coinValue = getSingleItemValue(item);

            if (coinValue == -1) {
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
        user.playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

        // Log deposit
        Transactions.builder()
                .type(TransactionType.DEPOSIT)
                .target(user.getUniqueId())
                .extra("coins=%s", coins)
                .amount(earned)
                .log();

        return 0;
    }

    private boolean isCoin(ItemStack item) {
        if (ItemStacks.isEmpty(item)
                || item.getType() != FtcItems.COIN_MATERIAL
        ) {
            return false;
        }

        var meta = item.getItemMeta();
        var lore = meta.lore();

        if (lore == null || lore.isEmpty()) {
            return false;
        }

        String plain = Text.plain(lore.get(0));

        return WORTH_PATTERN.matcher(plain.trim())
                .matches();
    }
}