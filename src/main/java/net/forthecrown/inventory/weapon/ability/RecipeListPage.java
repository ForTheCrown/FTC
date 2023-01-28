package net.forthecrown.inventory.weapon.ability;

import static net.forthecrown.inventory.weapon.ability.AbilityMenus.CURRENT_TYPE;
import static net.forthecrown.inventory.weapon.ability.AbilityMenus.RECIPE_PAGE;

import java.util.List;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.Slot;
import net.forthecrown.utils.inventory.menu.page.ListPage;
import net.forthecrown.utils.inventory.menu.page.MenuPage;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecipeListPage extends ListPage<ItemStack> {
  public static final Slot ADVANCEMENT_SLOT = Slot.of(4, 2);
  public static final Slot TRIAL_SLOT = Slot.of(0, 2);

  public RecipeListPage(MenuPage parent) {
    super(parent, RECIPE_PAGE);

    initMenu(Menus.builder(Menus.sizeFromRows(3), "Recipe contents"), true);
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    super.createMenu(builder);

    builder.add(
        TRIAL_SLOT,

        MenuNode.builder()
            .setItem((user, context) -> {
              var type = context.getOrThrow(CURRENT_TYPE);

              if (type.getTrialArea() == null) {
                return null;
              }

              return ItemStacks.builder(Material.IRON_AXE)
                  .setName("&eTry it out!")
                  .addLore("&7Try out the upgrade in a trial area")

                  .addLoreRaw(Component.empty())
                  .addLore("&7You'll be teleported to a trial area,")
                  .addLore("&7your items will be saved in a separate inventory")
                  .addLore("&7and returned to you, once you leave")
                  .build();
            })

            .setRunnable((user, context, click) -> {
              var type = context.getOrThrow(CURRENT_TYPE);
              type.enterTrialArea(user);
            })

            .build()
    );

    builder.add(
        ADVANCEMENT_SLOT,
        MenuNode.builder()
            .setItem((user, context) -> {
              var type = context.getOrThrow(CURRENT_TYPE);
              var adv = type.getAdvancement();

              if (adv == null) {
                return null;
              }

              var display = adv.getDisplay();

              if (display == null) {
                return null;
              }

              var icon = ItemStacks.toBuilder(display.icon());
              boolean done = user.getPlayer()
                  .getAdvancementProgress(adv)
                  .isDone();

              TextColor color = done
                  ? NamedTextColor.YELLOW
                  : NamedTextColor.GRAY;

              icon.setName(
                  Text.format("Requires {0}", color, adv.displayName())
              );

              if (done) {
                icon.addLore("&eAdvancement completed!");
              } else {
                icon.addLore("&7Not completed");
              }

              return icon.build();
            })

            .build()
    );
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
    return type.createDisplayItem(user);
  }

  @Override
  protected MenuNode createHeader() {
    return this;
  }
}