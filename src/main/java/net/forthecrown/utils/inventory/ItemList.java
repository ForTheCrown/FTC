package net.forthecrown.utils.inventory;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collection;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public interface ItemList extends List<ItemStack> {

  static ItemList of(Collection<ItemStack> items) {
    return new ItemArrayList(items);
  }

  default void removeAllMatching(Collection<ItemStack> items) {
    items.forEach(this::removeMatching);
  }

  default void removeMatching(ItemStack itemStack) {
    int remaining = itemStack.getAmount();
    var it = listIterator();

    while (it.hasNext()) {
      var i = it.next();

      if (!itemStack.isSimilar(i)) {
        continue;
      }

      int amount = i.getAmount();

      if (amount < remaining) {
        remaining -= amount;
        i.setAmount(0);
      } else {
        i.subtract(remaining);
        remaining = 0;
      }

      if (i.getAmount() <= 0) {
        it.remove();
      }

      if (remaining <= 0) {
        return;
      }
    }
  }

  default boolean containsAtLeastAll(Collection<ItemStack> items) {
    Object2IntMap<ItemStack> totalRequired = new Object2IntOpenHashMap<>();

    items.forEach(itemStack -> {
      totalRequired.compute(itemStack, (itemStack1, integer) -> {
        int amount = integer == null ? 0 : integer;
        amount += itemStack1.getAmount();
        return amount;
      });
    });

    for (var e: totalRequired.object2IntEntrySet()) {
      if (containsAtLeast(e.getKey(), e.getIntValue())) {
        continue;
      }

      return false;
    }

    return true;
  }

  default boolean containsAtLeast(ItemStack item) {
    return containsAtLeast(item, item.getAmount());
  }

  default boolean containsAtLeast(ItemStack item, int requiredAmount) {
    if (isEmpty()) {
      return false;
    }

    int foundCount = 0;

    for (var i: this) {
      if (item.isSimilar(i)) {
        foundCount += i.getAmount();
      }
    }

    return foundCount >= requiredAmount;
  }

}