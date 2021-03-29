package net.forthecrown.core.enums;

import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.utils.ComponentUtils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public enum ShopType {

    SELL_SHOP ("&a&l=[Sell]=", "&4&l=[Sell]=", false),
    BUY_SHOP ("&a&l=[Buy]=", "&4&l=[Buy]=", false),
    ADMIN_SELL_SHOP ("&b&l=[Sell]=", "&4&l=[Sell]=", true),
    ADMIN_BUY_SHOP ("&b&l=[Buy]=", "&4&l=[Buy]=", true);

    private final String inStock;
    private final String outStock;
    private final boolean isAdmin;

    ShopType(@NotNull String inStock, @NotNull String outStock, boolean isAdmin){
        this.inStock = inStock;
        this.outStock = outStock;
        this.isAdmin = isAdmin;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    @NotNull
    public String getInStockLabel(){
        return CrownUtils.translateHexCodes(inStock);
    }

    @NotNull
    public String getOutOfStockLabel() {
        return CrownUtils.translateHexCodes(outStock);
    }

    @NotNull
    public Component inStockLabel(){
        return ComponentUtils.convertString(inStock);
    }

    @NotNull
    public Component outOfStockLabel(){
        return ComponentUtils.convertString(outStock);
    }
}