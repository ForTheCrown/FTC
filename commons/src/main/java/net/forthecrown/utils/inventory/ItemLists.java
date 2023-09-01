package net.forthecrown.utils.inventory;

import com.google.common.collect.Iterators;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemLists {
  private ItemLists() {}

  public static ItemList cloneAllItems(Iterable<ItemStack> items) {
    ItemList result = newList();
    var it = Iterators.filter(items.iterator(), ItemStacks::notEmpty);

    while (it.hasNext()) {
      var n = it.next();
      result.add(n.clone());
    }

    return result;
  }

  public static ItemList newList() {
    return new ItemArrayList();
  }

  public static ItemList newList(ItemStack... items) {
    return new ItemArrayList(Arrays.asList(items));
  }

  public static ItemList newList(Collection<ItemStack> items) {
    return new ItemArrayList(items);
  }

  public static ItemList fromInventory(Inventory inventory) {
    return fromInventory(inventory, null);
  }

  public static ItemList fromInventory(
      Inventory inventory,
      @Nullable Predicate<ItemStack> predicate
  ) {
    ItemList list = new ItemArrayList();
    var it = ItemStacks.nonEmptyIterator(inventory);

    while (it.hasNext()) {
      var n = it.next();

      if (predicate == null || predicate.test(n)) {
        list.add(n);
      }
    }

    return list;
  }
}