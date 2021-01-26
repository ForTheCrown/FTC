package net.forthecrown.core.enums;

import net.forthecrown.core.FtcCore;

import javax.annotation.Nonnull;

public enum ShopType {

    SELL_SHOP ("&a&l=[Sell]=", "&4&l=[Sell]="),
    BUY_SHOP ("&a&l=[Buy]=", "&4&l=[Buy]="),
    ADMIN_SELL_SHOP ("&b&l=[Sell]=", "&4&l=[Sell]="),
    ADMIN_BUY_SHOP ("&b&l=[Buy]=", "&4&l=[Buy]=");

    private final String inStock;
    private final String outOfStock;

    ShopType(@Nonnull String firstLine, @Nonnull String outOfStockLine){
        this.inStock = firstLine;
        this.outOfStock = outOfStockLine;
    }

    @Nonnull
    public String getInStockLabel(){
        return FtcCore.translateHexCodes(inStock);
    }

    @Nonnull
    public String getOutOfStockLabel() {
        return FtcCore.translateHexCodes(outOfStock);
    }
}