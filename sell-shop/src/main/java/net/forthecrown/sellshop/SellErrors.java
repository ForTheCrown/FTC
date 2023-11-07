package net.forthecrown.sellshop;

import static net.forthecrown.command.Exceptions.create;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

public interface SellErrors {

  CommandSyntaxException NO_ITEM_TO_SELL = create("Not enough items to sell");

  CommandSyntaxException CANNOT_SELL_MORE = create("Cannot sell more, price would drop to 0");
}
