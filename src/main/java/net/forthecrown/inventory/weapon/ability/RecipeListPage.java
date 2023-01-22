package net.forthecrown.inventory.weapon.ability;

import static net.forthecrown.inventory.weapon.ability.AbilityMenus.CURRENT_TYPE;
import static net.forthecrown.inventory.weapon.ability.AbilityMenus.RECIPE_PAGE;

import java.util.List;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.page.ListPage;
import net.forthecrown.utils.inventory.menu.page.MenuPage;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecipeListPage extends ListPage<ItemStack> {

  public RecipeListPage(MenuPage parent) {
    super(parent, RECIPE_PAGE);

    initMenu(Menus.builder(Menus.sizeFromRows(3), "Recipe contents"), true);
  }

  @Override
  protected List<ItemStack> getList(User user, Context context) {
    var type = context.getOrThrow(CURRENT_TYPE);
    return type.getRecipe();
  }

  @Override
  protected ItemStack getItem(User user, ItemStack entry, Context context) {
    return entry.clone();
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user,
                                        @NotNull Context context
  ) {
    var type = context.getOrThrow(CURRENT_TYPE);
    return type.createDisplayItem().build();
  }

  @Override
  protected MenuNode createHeader() {
    return this;
  }
}