package net.forthecrown.inventory;

import net.forthecrown.core.module.OnEnable;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.inventory.weapon.RoyalSwordType;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.inventory.ItemStack;

public final class ExtendedItems {
    private ExtendedItems() {}

    public static final String TAG_CONTAINER = "special_item";
    public static final String TAG_TYPE = "type";
    public static final String TAG_DATA = "data";

    /** Item type which represents royal swords */
    public static final ExtendedItemType<RoyalSword> ROYAL_SWORD    = register(new RoyalSwordType());

    /** Item type which represents crowns */
    public static final ExtendedItemType<RoyalCrown> CROWN          = register(new CrownType());

    @OnEnable
    public static void init() {
        Registries.ITEM_TYPES.freeze();
    }

    private static <T extends ExtendedItemType> T register(T type) {
        return (T) Registries.ITEM_TYPES.register(type.getKey(), type).getValue();
    }

    public static ExtendedItemType getType(ItemStack itemStack) {
        for (var type: Registries.ITEM_TYPES) {
            if (type.get(itemStack) != null) {
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