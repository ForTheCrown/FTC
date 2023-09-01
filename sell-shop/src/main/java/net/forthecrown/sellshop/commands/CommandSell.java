package net.forthecrown.sellshop.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.Commands;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.sellshop.ItemSeller;
import net.forthecrown.sellshop.SellAmount;
import net.forthecrown.sellshop.SellProperties;
import net.forthecrown.sellshop.SellShop;
import net.forthecrown.user.User;
import org.bukkit.Material;

public class CommandSell extends FtcCommand {

  private final SellShop shop;
  private final SellMaterialArgument argument;

  public CommandSell(SellShop shop, SellMaterialArgument argument) {
    super("Sell");

    this.shop = shop;
    this.argument = argument;

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
          return sell(held.getType(), user, SellAmount.ALL);
        })

        .then(argument("mat", argument)
            .executes(c -> {
              Material material = c.getArgument("mat", Material.class);
              return sell(material, getUserSender(c), SellAmount.ALL);
            })
        );
  }

  private int sell(Material material, User user, SellAmount amount) throws CommandSyntaxException {
    user.set(SellProperties.SELL_AMOUNT, amount);
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
