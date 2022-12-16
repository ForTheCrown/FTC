package net.forthecrown.economy.sell;

import lombok.Getter;
import net.forthecrown.utils.inventory.menu.*;
import net.kyori.adventure.text.Component;

@Getter
public class SellShopMenu {
    /* ----------------------------- CONSTANTS ------------------------------ */

    private static final int
            HEADER_SLOT      = Slot.toIndex(4, 0),
            SLOT_LORE_FILTER = HEADER_SLOT + 1,
            SLOT_NAME_FILTER = HEADER_SLOT - 1,
            SLOT_COMPACT     = Slot.COLUMN_SIZE - 1;

    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    private final Menu inventory;
    private final MenuNodeItem button;
    private final ItemPriceMap priceMap;

    /* ----------------------------- CONSTRUCTOR ------------------------------ */

    public SellShopMenu(MenuNodeItem button, ItemPriceMap priceMap, int size, Component title, SellShop shops) {
        this.button = button;
        this.priceMap = priceMap;

        this.inventory = createMenu(this, size, title, shops);
    }

    /* ----------------------------- STATIC METHODS ------------------------------ */

    /**
     * Creates a sellshop's menu
     * @param sellShop The shop to create a menu for
     * @param size The size of the menu
     * @param title The menu's title
     * @param shops The shop manager instance
     * @return The created menu
     */
    private static Menu createMenu(SellShopMenu sellShop, int size, Component title, SellShop shops) {
        var builder = Menus.builder(size, title);

        builder
                .addBorder()

                .add(Slot.ZERO, SellShopNodes.previousPage(shops))

                .add(SLOT_NAME_FILTER, SellShopNodes.SELLING_NAMED)
                .add(SLOT_LORE_FILTER, SellShopNodes.SELLING_LORE)
                .add(SLOT_COMPACT,     SellShopNodes.COMPACT_TOGGLE)

                .add(HEADER_SLOT,
                        MenuNode.builder()
                                .setItem(sellShop.button)
                                .build()
                );

        for (var e: SellShopNodes.SELL_AMOUNT_NODES.entrySet()) {
            builder.add(e.getKey(), e.getValue());
        }

        for (var e: sellShop.priceMap) {
            builder.add(e.getInventoryIndex(), SellShopNodes.sellNode(e));
        }

        return builder.build();
    }
}