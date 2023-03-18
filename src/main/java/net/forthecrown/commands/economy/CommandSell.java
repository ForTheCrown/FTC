package net.forthecrown.commands.economy;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.sell.ItemSeller;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.data.SellAmount;
import net.forthecrown.user.property.Properties;
import org.bukkit.Material;

public class CommandSell extends FtcCommand {

  public CommandSell() {
    super("Sell");

    setDescription("Sells an item your holding");
    simpleUsages();

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /Sell
   *
   * Permissions used:
   *
   * Main Author:
   */

  @Override
  public boolean test(CommandSource source) {
    return super.test(source)
        && (source.textName().startsWith("__") || source.isOp());
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          var user = getUserSender(c);
          var held = Commands.getHeldItem(user.getPlayer());

          return sell(held.getType().name(), user, SellAmount.ALL);
        })

        .then(argument("mat", StringArgumentType.word())
            .suggests((context, builder) -> {
              var map = Economy.get().getSellShop().getPriceMap();

              return Completions.suggest(
                  builder,
                  map::keyIterator
              );
            })

            .executes(c -> {
              String s = c.getArgument("mat", String.class);
              return sell(
                  s, getUserSender(c), SellAmount.ALL
              );
            })
        );
  }

  private int sell(String matString, User user, SellAmount amount)
      throws CommandSyntaxException
  {
    Material material = Material.matchMaterial(matString);

    if (material == null) {
      throw Exceptions.format(
          "Unknown material: {0}",
          matString
      );
    }

    user.set(Properties.SELL_AMOUNT, amount);
    var shop = Economy.get().getSellShop();
    var data = shop.getPriceMap().get(material);

    if (data == null) {
      throw Exceptions.format(
          "{0} is not a sellable material",
          material
      );
    }

    var seller = ItemSeller.inventorySell(
        user,
        material,
        data
    );

    seller.run(true);
    return 0;
  }
}