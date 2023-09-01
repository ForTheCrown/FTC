package net.forthecrown.command.arguments;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.Commands;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.utils.inventory.ItemList;
import net.forthecrown.utils.inventory.ItemLists;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.inventory.ItemStack;

public interface ItemListResult {

  ItemListResult INVENTORY = source -> {
    var player = source.asPlayer();
    ItemList list = ItemLists.fromInventory(player.getInventory(), ItemStacks::notEmpty);

    if (list.isEmpty()) {
      throw Exceptions.create("Your inventory is empty");
    }

    return list;
  };

  ItemListResult HELD_ITEM = source -> {
    ItemStack held = Commands.getHeldItem(source.asPlayer());
    return ItemLists.newList(held);
  };

  ItemList get(CommandSource source) throws CommandSyntaxException;
}
