package net.forthecrown.economy.shops;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a shop's type
 */
public enum ShopType {

    SELL_SHOP (ShopManager.SELL_LABEL, ShopManager.NORMAL_STYLE, false),
    BUY_SHOP (ShopManager.BUY_LABEL, ShopManager.NORMAL_STYLE, false),
    ADMIN_SELL_SHOP (ShopManager.SELL_LABEL, ShopManager.ADMIN_STYLE, true),
    ADMIN_BUY_SHOP (ShopManager.BUY_LABEL, ShopManager.ADMIN_STYLE, true);

    private final Component inStock;
    private final Component outStock;
    private final boolean isAdmin;
    private final boolean buyType;

    ShopType(@NotNull String label, @NotNull Style style, boolean isAdmin){
        buyType = label.contains("Buy");
        inStock = Component.text(label).style(style);
        outStock = Component.text(label).style(ShopManager.OUT_OF_STOCK_STYLE);

        this.isAdmin = isAdmin;
    }

    /**
     * Gets the in stock label for the type
     * @return The in stock label of the type
     */
    @NotNull
    public Component inStockLabel(){
        return inStock;
    }

    /**
     * Gets the out of stock label of the type
     * @return The out of stock label for the type
     */
    @NotNull
    public Component outOfStockLabel(){
        return outStock;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public boolean isBuyType() {
        return buyType;
    }
}