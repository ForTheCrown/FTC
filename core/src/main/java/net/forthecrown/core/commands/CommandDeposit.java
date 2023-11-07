package net.forthecrown.core.commands;

import com.google.common.collect.Iterators;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Iterator;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.Coins;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.inventory.ItemStack;

public class CommandDeposit extends FtcCommand {

  public CommandDeposit() {
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
  public void populateUsages(UsageFactory factory) {
    factory.usage("")
        .addInfo("Deposits all held coins");

    factory.usage("all")
        .addInfo("Deposits all coins in your inventory");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
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

  private int depositCoins(User user, Iterator<ItemStack> it) throws CommandSyntaxException {
    int earned = Coins.deposit(user, it, -1);

    if (earned < 1) {
      throw Exceptions.create("You must be holding the coins you wish to deposit.");
    }

    return 0;
  }
}