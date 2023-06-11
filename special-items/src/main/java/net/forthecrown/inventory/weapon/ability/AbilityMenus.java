package net.forthecrown.inventory.weapon.ability;

import static net.kyori.adventure.text.Component.text;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import net.forthecrown.command.Exceptions;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.menu.ClickContext;
import net.forthecrown.menu.MenuBuilder;
import net.forthecrown.menu.MenuFlag;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Menus;
import net.forthecrown.menu.page.MenuPage;
import net.forthecrown.registry.Holder;
import net.forthecrown.text.Text;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.context.ContextOption;
import net.forthecrown.utils.context.ContextSet;
import net.forthecrown.utils.inventory.ItemArrayList;
import net.forthecrown.utils.inventory.ItemList;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.Slot;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AbilityMenus extends MenuPage {
  public static final Set<Slot> SLOTS = Set.of(
      Slot.of(5, 1),
      Slot.of(5, 3),
      Slot.of(3, 0),
      Slot.of(3, 4),
      Slot.of(1, 3),
      Slot.of(1, 1)
  );

  public static final Slot SWORD_SLOT = Slot.of(3, 2);

  public static final Slot ABILITY_SLOT = SWORD_SLOT.add(0, 1);

  public static final Slot RECIPE_LIST_SLOT = Slot.of(8, 0);

  public static final Slot ANIM_TOGGLE_SLOT = RECIPE_LIST_SLOT.add(0, 1);

  public static final ImmutableSet<Slot> RESERVED_SLOTS
      = ImmutableSet.<Slot>builder()
      .addAll(SLOTS)
      .add(SWORD_SLOT)
      .build();

  private static final ItemStack NO_ABILITY_ITEM
      = ItemStacks.builder(Material.RED_STAINED_GLASS_PANE)
      .setName("&cNo upgrade")
      .build();

  public static final ContextSet SET = ContextSet.create();

  public static final ContextOption<Integer> RECIPE_PAGE
      = SET.newOption(0);

  public static final ContextOption<WeaponAbilityType> CURRENT_TYPE
      = SET.newOption();

  public static final ContextOption<Location> SLIME_POSITION
      = SET.newOption();

  public static final ContextOption<Boolean> WARNED
      = SET.newOption(false);

  @Getter
  private static final AbilityMenus instance = new AbilityMenus();

  private final AbilityListPage abilityList;

  @Getter
  private boolean opened;

  public AbilityMenus() {
    abilityList = new AbilityListPage(this);

    Menus.makeUnstackable(NO_ABILITY_ITEM);

    var builder = Menus.builder(Menus.sizeFromRows(5))
        .setTitle("Weapon upgrades")
        .addFlag(MenuFlag.PREVENT_ITEM_STACKING)
        .addFlag(MenuFlag.ALLOW_ITEM_MOVING)
        .addFlag(MenuFlag.ALLOW_SHIFT_CLICKING);

    initMenu(builder, false);
  }

  public void open(User user, Location location) {
    getMenu().open(
        user,
        SET.createContext().set(SLIME_POSITION, location)
    );
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    List<Slot> fillSkips = new ArrayList<>(RESERVED_SLOTS);
    fillSkips.add(RECIPE_LIST_SLOT);
    fillSkips.add(ABILITY_SLOT);
    fillSkips.add(ANIM_TOGGLE_SLOT);

    // Fill inventory with glass
    for (int i = 0; i < builder.getSize(); i++) {
      Slot s = Slot.of(i);

      // Keep reserved slots, and ability slot empty
      if (fillSkips.contains(s)) {
        continue;
      }

      builder.add(s,
          MenuNode.builder()
              .setRunnable((user, context) -> {
                context.cancelEvent(true);
                context.setCooldownTime(0);
              })
              .setItem(Menus.defaultBorderItem())
              .build()
      );
    }

    builder.add(RECIPE_LIST_SLOT, abilityList);

    builder.add(ANIM_TOGGLE_SLOT,
        MenuNode.builder()
            .setItem((user, context) -> {
              boolean playAnim = !user.get(Properties.SKIP_ABILITY_ANIM);

              var iBuilder = ItemStacks.builder(
                  playAnim
                      ? Material.GLOW_ITEM_FRAME
                      : Material.ITEM_FRAME
              );

              iBuilder.setName("&eToggle animation skipping");

              if (playAnim) {
                iBuilder.addLore("&7Click to disable animations")
                    .addLore("&8Currently, an animation sill play when")
                    .addLore("&8setting or upgrading an ability.");
              } else {
                iBuilder.addLore("&7Click to enable animations")
                    .addLore("&8Currently, the animation that plays when")
                    .addLore("&8setting or upgrading an ability will be skipped");
              }

              return iBuilder.build();
            })

            .setRunnable((user, context, click) -> {
              click.cancelEvent(true);
              user.flip(Properties.SKIP_ABILITY_ANIM);

              // Don't reload inventory, might remove items they placed in it,
              // instead just modify this option's item 1 tick after the click
              // happens
              Tasks.runLater(() -> {
                var item = click.getNode().createItem(user, context);

                assert item != null
                    : "Animation skip toggle produced null item";

                Menus.makeUnstackable(item);

                var view = click.getView();
                view.setItem(click.getRawSlot(), item);
              }, 1L);
            })

            .build()
    );

    // Place valid recipe check nodes in the reserved spaces
    RESERVED_SLOTS.forEach(slot -> {
      builder.add(slot,
          MenuNode.builder()
              .setRunnable((user, context, click) -> {
                click.cancelEvent(false);
                checkValidRecipe(click.getInventory(), user);
              })
              .build()
      );
    });

    builder
        // Slot below sword, u click on it, and it applies the ability to the
        // item or upgrades the existing one
        .add(ABILITY_SLOT,
            MenuNode.builder()
                .setRunnable(AbilityMenus::onCraftClick)
                .build()
        )

        // When a user clicks in their inventory, they might shift-click
        // something into the menu, so update the menu and perform some special
        // logic if a sword is shift-clicked
        .setExternalClickCallback((click, context) -> {
          var view = click.getView();
          var item = view.getItem(click.getRawSlot());

          // If a sword is being shift-clicked, move it to the sword slot,
          // instead of to the first open slot
          if (click.getType() == ClickType.SHIFT_LEFT
              && ExtendedItems.ROYAL_SWORD.get(item) != null
          ) {
            int swordSlot = SWORD_SLOT.getIndex();
            var sword = view.getItem(swordSlot);

            view.setItem(swordSlot, item);
            view.setItem(click.getRawSlot(), sword);

            click.cancelEvent(true);
          }

          checkValidRecipe(click.getMenuInventory(), click.getUser());
        })

        // Place the no ability item in the inventory with a correct
        // explanation as to why it's there in the item's name
        .setOpenCallback((user, context, inventory) -> {
          opened = true;
          checkValidRecipe(inventory, user);
        })

        // Give all items back to user when inventory is closed
        .setCloseCallback((inventory, user, reason) -> {
          opened = false;

          for (var s: RESERVED_SLOTS) {
            ItemStack item = inventory.getItem(s);

            if (ItemStacks.isEmpty(item)) {
              continue;
            }

            // Can't use should only be the reason when the inventory
            // was closed to display the animation
            if (s.equals(SWORD_SLOT) && reason == Reason.CANT_USE) {
              continue;
            }

            Util.giveOrDropItem(
                user.getInventory(),
                user.getLocation(),
                item
            );
          }
        })
        .build();
  }

  @Override
  protected void addBorder(MenuBuilder builder) {}

  @Override
  public @Nullable ItemStack createItem(@NotNull User user,
                                        @NotNull Context context
  ) {
    return ItemStacks.builder(Material.BREWING_STAND)
        .setName("&eUpgrade crafting menu")
        .addLore("&7Back to main menu")
        .build();
  }

  private static void onCraftClick(User user,
                                   Context context,
                                   ClickContext click
  ) throws CommandSyntaxException {
    click.cancelEvent(true);
    var matching = matchingAbility(click.getInventory(), user);

    if (matching.left().isEmpty()) {
      // Left empty == right present, no need for isPresent check
      throw Exceptions.format(matching.right().get());
    }

    Holder<WeaponAbilityType> abilityType = matching.left().get();
    ItemStack item = click.getInventory().getItem(SWORD_SLOT.getIndex());
    RoyalSword sword = ExtendedItems.ROYAL_SWORD.get(item);

    assert sword != null : "Sword is somehow null";

    WeaponAbility existingAbility = sword.getAbility();

    // Warn if overriding existing ability
    if (existingAbility != null
        && existingAbility.getType() != abilityType.getValue()
        && !context.getOrThrow(WARNED)
    ) {
      context.set(WARNED, true);

      user.sendMessage(
          Text.format(
              "Your sword has the {0} upgrade, "
                  + "adding {1} will override the first upgrade!"
                  + "\n&eClick again to override upgrade",

              existingAbility.getType().fullDisplayName(user),
              abilityType.getValue().fullDisplayName(user)
          )
      );

      return;
    }

    // Apply ability upgrade or apply ability to sword if existing
    // one is null or different
    if (existingAbility == null
        || !existingAbility.getType().equals(abilityType.getValue())
    ) {
      sword.setAbility(
          abilityType.getValue()
              .create(user, sword.getTotalUses(abilityType))
      );

      user.sendMessage(
          Text.format("Set sword's upgrade to &e{0}&r.",
              NamedTextColor.GOLD,
              sword.getAbility().displayName()
          )
      );
    }

    sword.update(item);

    // Remove all items used in ability recipe
    var items = abilityType.getValue().getRecipe();
    ItemList menuItems = new ItemArrayList();

    for (var slot: SLOTS) {
      var ingredient = click.getInventory().getItem(slot);

      if (ItemStacks.isEmpty(ingredient)) {
        continue;
      }

      menuItems.add(ingredient);
    }

    menuItems.removeAllMatching(items);

    boolean playAnim = !user.get(Properties.SKIP_ABILITY_ANIM);

    if (playAnim) {
      AbilityAnimation.getInstance().start(
          item,
          context.getOrThrow(SLIME_POSITION),
          user
      );
    } else {
      // Check if ability upgrade or other stuff can be
      // applied again
      checkValidRecipe(click.getInventory(), user);
    }
  }

  private static void checkValidRecipe(Inventory inventory, User user) {
    // Delay by a single tick because when the click event is fired, the
    // item that'll have been placed or removed from the inventory, won't
    // have moved until after the event
    Tasks.runLater(() -> {
      var first = matchingAbility(inventory, user);

      if (first.left().isEmpty()) {
        var item = ItemStacks.toBuilder(NO_ABILITY_ITEM.clone());
        // isPresent check not required, if left is empty, right will be there
        item.setName(text(first.right().get(), NamedTextColor.RED));

        inventory.setItem(ABILITY_SLOT.getIndex(), item.build());
        return;
      }

      Holder<WeaponAbilityType> ability = first.left().get();
      var type = ability.getValue();

      var abilityItem = type.getItem();
      Menus.makeUnstackable(abilityItem);

      inventory.setItem(ABILITY_SLOT.getIndex(), abilityItem);
    }, 1L);
  }

  private static Either<Holder<WeaponAbilityType>, String> matchingAbility(
      Inventory inventory,
      User user
  ) {
    Mutable<WeaponAbility> existing = new MutableObject<>();
    var swordItem = inventory.getItem(SWORD_SLOT.getIndex());

    // If no sword set
    if (ItemStacks.isEmpty(swordItem)) {
      return Either.right("No sword in middle slot");
    }

    RoyalSword sword = ExtendedItems.ROYAL_SWORD.get(swordItem);

    if (sword == null) {
      return Either.right("Non-sword in middle slot");
    }

    if (!user.getUniqueId().equals(sword.getOwner())) {
      return Either.right("You do not own the sword");
    }

    existing.setValue(sword.getAbility());
    ItemList menuItems = new ItemArrayList();

    for (var s: SLOTS) {
      var item = inventory.getItem(s.getIndex());

      if (ItemStacks.isEmpty(item)) {
        continue;
      }

      menuItems.add(item);
    }

    Optional<Either<Holder<WeaponAbilityType>, String>> first
        = SwordAbilityManager.getInstance().getRegistry()
        .entries()
        .stream()
        .filter(holder -> {
          var val = holder.getValue();
          var advancement = val.getAdvancement();

          if (advancement != null) {
            boolean hasAdvancement = user.getPlayer()
                .getAdvancementProgress(advancement)
                .isDone();

            if (!hasAdvancement) {
              return false;
            }
          }

          ImmutableList<ItemStack> recipe = val.getRecipe();
          return menuItems.containsAtLeastAll(recipe);
        })
        .findFirst()
        .map(holder -> {
          var ability = existing.getValue();

          if (ability != null && ability.getType().equals(holder.getValue())) {
            return Either.right("Ability already on sword");
          }

          return Either.left(holder);
        });

    return first.orElse(Either.right("No matching recipe found"));
  }
}