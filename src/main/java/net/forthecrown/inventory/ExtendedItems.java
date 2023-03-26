package net.forthecrown.inventory;

import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.dungeons.enchantments.FtcEnchants;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.inventory.weapon.RoyalSwordType;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.inventory.ItemStack;

public final class ExtendedItems {
  private ExtendedItems() {}

  public static final String TAG_CONTAINER = "special_item";
  public static final String TAG_TYPE = "type";
  public static final String TAG_DATA = "data";

  public static final Registry<ExtendedItemType> REGISTRY
      = Registries.newFreezable();

  /**
   * Item type which represents royal swords
   */
  public static final ExtendedItemType<RoyalSword> ROYAL_SWORD
      = register(new RoyalSwordType());

  /**
   * Item type which represents crowns
   */
  public static final ExtendedItemType<RoyalCrown> CROWN
      = register(new CrownType());

  static {
    REGISTRY.freeze();
  }

  @SuppressWarnings("unchecked")
  private static <T extends ExtendedItemType<?>> T register(T type) {
    return (T) REGISTRY.register(type.getKey(), type).getValue();
  }

  public static ExtendedItemType<?> getType(ItemStack itemStack) {
    for (var type : REGISTRY) {
      if (type.get(itemStack) != null) {
        fixInvRemain(type, itemStack);
        return type;
      }
    }

    return null;
  }

  public static void fixLegacyIfNeeded(ItemStack item) {
    item.editMeta(meta -> {
      if (ItemStacks.hasTagElement(meta, ROYAL_SWORD.getKey())) {
        ExtendedItemFix.fixSword(meta);
      } else if (ItemStacks.hasTagElement(meta, CROWN.getKey())) {
        ExtendedItemFix.fixCrown(meta);
      }
    });
  }

  public static void fixInvRemain(ExtendedItemType<?> type, ItemStack item) {
    if (type != ROYAL_SWORD && type != CROWN) {
      return;
    }

    item.addEnchantment(FtcEnchants.SOUL_BOND, 1);
  }

  public static boolean shouldRemainInInventory(ItemStack itemStack) {
    var type = getType(itemStack);

    if (type == null) {
      return false;
    }

    return type.shouldRemainInInventory();
  }

  public static boolean isSpecial(ItemStack itemStack) {
    if (ItemStacks.isEmpty(itemStack)) {
      return false;
    }

    fixLegacyIfNeeded(itemStack);
    var meta = itemStack.getItemMeta();

    return ItemStacks.hasTagElement(meta, TAG_CONTAINER);
  }
}