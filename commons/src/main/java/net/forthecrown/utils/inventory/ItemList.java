package net.forthecrown.utils.inventory;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ItemList extends List<ItemStack> {
  int REMOVE_ALL = -1;

  default void removeAllMatching(Collection<ItemStack> items) {
    items.forEach(this::removeMatching);
  }

  default int removeMatching(ItemStack itemStack) {
    return removeMatching(itemStack, itemStack.getAmount());
  }

  default int removeItems(int amount) {
    return removeMatching(null, amount);
  }

  default int removeMatching(
      @Nullable ItemStack itemStack,
      final int removeAmount
  ) {
    Preconditions.checkArgument(
        removeAmount > 0 || removeAmount == REMOVE_ALL,
        "Amount to remove must be above 0, or -1"
    );

    int total = totalItemCount();
    if ((removeAmount == REMOVE_ALL || removeAmount >= total)
        && itemStack == null
    ) {
      forEach(i -> i.setAmount(0));
      clear();
      return total;
    }

    int remaining = removeAmount;
    int removed = 0;

    var it = listIterator();

    while (it.hasNext()) {
      var i = it.next();

      if (itemStack != null && !itemStack.isSimilar(i)) {
        continue;
      }

      int amount = i.getAmount();

      if (removeAmount == REMOVE_ALL) {
        removed += amount;

        i.setAmount(0);
        it.remove();

        continue;
      }

      if (amount < remaining) {
        remaining -= amount;
        removed += amount;
        i.setAmount(0);
      } else {
        i.subtract(remaining);
        remaining = 0;
        removed += amount - i.getAmount();
      }

      if (i.getAmount() <= 0) {
        it.remove();
      }

      if (remaining <= 0) {
        return removed;
      }
    }

    return removed;
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

  default int totalItemCount() {
    return stream()
        .filter(Objects::nonNull)
        .reduce(
            0,
            (amount, item) -> amount + item.getAmount(),
            Integer::sum
        );
  }
}