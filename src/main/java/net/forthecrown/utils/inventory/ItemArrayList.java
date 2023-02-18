package net.forthecrown.utils.inventory;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import org.bukkit.inventory.ItemStack;

public class ItemArrayList
    extends ObjectArrayList<ItemStack>
    implements ItemList
{
  public ItemArrayList() {
  }

  public ItemArrayList(Collection<? extends ItemStack> c) {
    super(c);
  }
}