package net.forthecrown.inventory.weapon.ability.menu;

import static net.forthecrown.inventory.weapon.ability.menu.AbilityMenus.CURRENT_TYPE;
import static net.forthecrown.inventory.weapon.ability.menu.AbilityMenus.RECIPE_PAGE;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import net.forthecrown.inventory.weapon.WeaponAbilities;
import net.forthecrown.inventory.weapon.ability.WeaponAbilityType;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.ClickContext;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.page.ListPage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AbilityListPage extends ListPage<WeaponAbilityType> {
  private final RecipeListPage recipeList;

  public AbilityListPage(AbilityMenus parent) {
    super(parent, RECIPE_PAGE);
    recipeList = new RecipeListPage(this);

    initMenu(
        Menus.builder(Menus.sizeFromRows(4), "Known recipes"),
        true
    );
  }

  @Override
  protected List<WeaponAbilityType> getList(User user, Context context) {
    return WeaponAbilities.REGISTRY
        .values()
        .stream()
        .filter(type -> type.test(user))
        .collect(Collectors.toList());
  }

  @Override
  protected ItemStack getItem(User user,
                              WeaponAbilityType entry,
                              Context context
  ) {
    return entry.createDisplayItem().build();
  }

  @Override
  protected void onClick(User user,
                         WeaponAbilityType entry,
                         Context context,
                         ClickContext click
  ) throws CommandSyntaxException {
    context.set(CURRENT_TYPE, entry);
    context.set(RECIPE_PAGE, 0);
    recipeList.onClick(user, context, click);
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user,
                                        @NotNull Context context
  ) {
    return ItemStacks.builder(Material.BOOK)
        .setName("&eKnown abilities")
        .addLore("&7Click to see all known abilities")
        .addLore("&7and their recipes")
        .build();
  }

  @Override
  protected MenuNode createHeader() {
    return this;
  }
}