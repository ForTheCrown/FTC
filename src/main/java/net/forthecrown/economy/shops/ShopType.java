package net.forthecrown.economy.shops;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a shop's type
 */
public enum ShopType {

    /* ----------------------------- SELL TYPES ------------------------------ */

    /**
     * The regular sell type where a player
     * gives a shop items in exchange for
     * currency
     */
    SELL (
            SignShops.SELL_LABEL,
            SignShops.NORMAL_STYLE,
            Interactions.SELL
    ),

    /**
     * A sell type where the inventory of
     * the shop doesn't change and can be
     * sold to forever...
     * <p>
     * because the server just prints
     * the money like the federal reserve...
     * sorry
     */
    ADMIN_SELL (
            SignShops.SELL_LABEL,
            SignShops.ADMIN_STYLE,
            Interactions.ADMIN_SELL
    ),

    /* ----------------------------- BUY TYPES ------------------------------ */

    /**
     * Shop type where a player will give the shop Rhines
     * in exchange for items
     */
    BUY (
            SignShops.BUY_LABEL,
            SignShops.NORMAL_STYLE,
            Interactions.BUY
    ),

    /**
     * Shop type where the shop never runs out of materials and
     * to sell to the player
     */
    ADMIN_BUY (
            SignShops.BUY_LABEL,
            SignShops.ADMIN_STYLE,
            Interactions.ADMIN_BUY
    );

    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    /**
     * The shop label to display when the
     * shop is in stock
     * @see SignShop#inStock()
     */
    @Getter
    private final Component stockedLabel;

    /**
     * The label to display when the
     * shop is out of stock
     * @see SignShop#inStock()
     */
    @Getter
    private final Component unStockedLabel;

    /**
     * Determines if this type is an admin type or not
     */
    @Getter
    private final boolean admin;

    /**
     * Determines if this type buys from their customer
     * or sells to them
     */
    @Getter
    private final boolean buyType;

    /**
     * The interaction shops of this type will have with customers
     */
    @Getter
    private final ShopInteraction interaction;

    /* ----------------------------------------------------------- */

    ShopType(@NotNull String label, @NotNull Style style, ShopInteraction interaction) {
        // Auto-detect buy type and if this is an
        // admin type
        this.buyType = label.contains(SignShops.BUY_LABEL);
        this.admin = name().contains("ADMIN");

        // Create labels
        this.stockedLabel = Component.text(label).style(style);
        this.unStockedLabel = Component.text(label).style(SignShops.OUT_OF_STOCK_STYLE);

        this.interaction = interaction;
    }
}