package net.forthecrown.core.enums;

import net.forthecrown.core.CrownUtils;

import javax.annotation.Nonnull;

public enum ShopType {

    SELL_SHOP ("&a&l=[Sell]=", "&4&l=[Sell]="),
    BUY_SHOP ("&a&l=[Buy]=", "&4&l=[Buy]="),
    ADMIN_SELL_SHOP ("&b&l=[Sell]=", "&4&l=[Sell]="),
    ADMIN_BUY_SHOP ("&b&l=[Buy]=", "&4&l=[Buy]=");

    private final String inStock;
    private final String outStock;

    ShopType(@Nonnull String inStock, @Nonnull String outStock){
        this.inStock = inStock;
        this.outStock = outStock;
    }

    @Nonnull
    public String getInStockLabel(){
        return CrownUtils.translateHexCodes(inStock);
    }

    @Nonnull
    public String getOutOfStockLabel() {
        return CrownUtils.translateHexCodes(outStock);
    }
}