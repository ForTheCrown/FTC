package net.forthecrown.core.enums;

import net.forthecrown.core.ShopManager;
import net.forthecrown.core.utils.ComponentUtils;
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

    ShopType(@NotNull String label, @NotNull Style style, boolean isAdmin){
        inStock = Component.text(label).style(style);
        outStock = Component.text(label).style(ShopManager.OUT_OF_STOCK_STYLE);

        this.isAdmin = isAdmin;
    }

    /**
     * Gets if the shop is admin type shop
     * @return Whether the type is admin or not
     */
    public boolean isAdmin() {
        return isAdmin;
    }

    /**
     * Gets the in stock label of the sign
     * @return The in stock label of the sign
     * @deprecated In favour of {@link ShopType#inStockLabel()}
     */
    @NotNull
    @Deprecated
    public String getInStockLabel(){
        return ComponentUtils.getString(inStock);
    }

    /**
     * Gets the out of stock label of the type
     * @return The sign label of the shop while it's out of stock
     * @deprecated In favour of {@link ShopType#outOfStockLabel()}
     */
    @NotNull
    @Deprecated
    public String getOutOfStockLabel() {
        return ComponentUtils.getString(outStock);
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
}