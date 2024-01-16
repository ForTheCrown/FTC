package net.forthecrown.sellshop.loader;

import com.mojang.serialization.DataResult;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Menus;
import net.forthecrown.menu.page.MenuPage;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.Results;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

class LoadingPage {

  static final int UNSET_SIZE = -1;

  int size = UNSET_SIZE;

  Component title;
  Component[] desc;

  Material headerItem;
  ItemStack border;
  MenuNode[] nodes = new MenuNode[Menus.MAX_INV_SIZE];

  boolean commandAccessible = false;

  void extend(LoadingPage parent) {
    if (size == UNSET_SIZE) {
      size = parent.size;
    }

    if (headerItem != null) {
      headerItem = parent.headerItem;
    }

    if (desc != null) {
      desc = parent.desc;
    }

    if (ItemStacks.isEmpty(border)) {
      border = parent.border;
    }

    if (title == null) {
      title = parent.title;
    }

    for (int i = 0; i < parent.nodes.length; i++) {
      MenuNode parentNode = parent.nodes[i];
      MenuNode thisNode = nodes[i];

      if (parentNode == null) {
        continue;
      }

      if (thisNode == null) {
        nodes[i] = parentNode;
      }
    }
  }

  DataResult<SellShopPage> build(MenuPage parent) {
    if (size == UNSET_SIZE) {
      return Results.error("Inventory size unset");
    }
    if (!Menus.isValidSize(size)) {
      return Results.error("Invalid inventory size: %s", size);
    }

    SellShopPage page = new SellShopPage(
        parent,
        border,
        size,
        title,
        desc,
        headerItem,
        nodes,
        commandAccessible
    );

    return Results.success(page);
  }
}
