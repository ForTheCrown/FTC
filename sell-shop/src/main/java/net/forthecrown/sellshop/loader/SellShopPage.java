package net.forthecrown.sellshop.loader;

import net.forthecrown.menu.MenuBuilder;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Menus;
import net.forthecrown.menu.page.MenuPage;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SellShopPage extends MenuPage {

  private final ItemStack borderItem;
  private final int size;

  private final boolean commandAccessible;

  private final Component title;
  private final Component[] desc;
  private final Material headerItem;

  private final MenuNode[] nodes;

  public SellShopPage(
      MenuPage parent,
      ItemStack borderItem,
      int size,
      Component title,
      Component[] desc,
      Material headerItem,
      MenuNode[] nodes,
      boolean commandAccessible
  ) {
    super(parent);
    this.borderItem = borderItem;
    this.size = size;
    this.title = title;
    this.desc = desc;
    this.headerItem = headerItem;
    this.nodes = nodes;
    this.commandAccessible = commandAccessible;
  }

  public void initiate() {
    var builder = Menus.builder(size, title);
    initMenu(builder, true);
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user, @NotNull Context context) {
    if (headerItem == null) {
      return null;
    }
    var builder = ItemStacks.builder(headerItem);
    builder.setName(title);

    if (desc != null) {
      for (Component component : desc) {
        if (component == null) {
          continue;
        }
        builder.addLore(component);
      }
    }

    return builder.build();
  }

  @Override
  protected MenuNode createHeader() {
    if (headerItem == null) {
      return null;
    }
    return super.createHeader();
  }

  @Override
  protected void addBorder(MenuBuilder builder) {
    if (ItemStacks.isEmpty(borderItem)) {
      return;
    }
    builder.addBorder(borderItem);
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    for (int i = 0; i < nodes.length; i++) {
      MenuNode node = nodes[i];
      if (node == null) {
        continue;
      }
      builder.add(i, node);
    }
  }
}
