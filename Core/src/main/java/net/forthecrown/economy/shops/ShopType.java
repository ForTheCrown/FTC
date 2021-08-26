package net.forthecrown.economy.shops;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a shop's type
 */
public enum ShopType {

    SELL(ShopManager.SELL_LABEL, ShopManager.NORMAL_STYLE, false, new SellInteraction()),
    BUY(ShopManager.BUY_LABEL, ShopManager.NORMAL_STYLE, false, new BuyInteraction()),
    ADMIN_SELL(ShopManager.SELL_LABEL, ShopManager.ADMIN_STYLE, true, new AdminSellInteraction()),
    ADMIN_BUY(ShopManager.BUY_LABEL, ShopManager.ADMIN_STYLE, true, new AdminBuyInteraction());

    private final Component inStock;
    private final Component outStock;
    private final boolean isAdmin;
    private final boolean buyType;
    private final ShopInteraction interaction;

    ShopType(@NotNull String label, @NotNull Style style, boolean isAdmin, ShopInteraction interaction){
        this.buyType = label.contains(ShopManager.BUY_LABEL);
        this.inStock = Component.text(label).style(style);
        this.outStock = Component.text(label).style(ShopManager.OUT_OF_STOCK_STYLE);

        this.interaction = interaction;
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

    /**
     * Gets whether this is an admin shop type
     * @return Whether this is an admin shop type
     */
    public boolean isAdmin() {
        return isAdmin;
    }

    /**
     * Gets whether this type is meant for selling or buying
     * @return Yeah, read the above line bucka roo
     */
    public boolean isBuyType() {
        return buyType;
    }

    /**
     * Gets the type's interaction type
     * @return The type's interaction type
     */
    public ShopInteraction getInteraction() {
        return interaction;
    }
}