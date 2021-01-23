package net.forthecrown.core.enums;

import net.forthecrown.core.FtcCore;

public enum ShopType {

    SELL_SHOP ("&a&l=[Sell]="),
    BUY_SHOP ("&a&l=[Buy]=", "&4&l=[Buy]="),
    ADMIN_SELL_SHOP ("&b&l=[Sell]="),
    ADMIN_BUY_SHOP ("&b&l=[Buy]=", "&4&l=[Buy]=");

    private final String inStock;
    private final String outOfStock;

    ShopType(String firstLine){
        this.inStock = firstLine;
        outOfStock = null;
    }
    ShopType(String firstLine, String outOfStockLine){
        this.inStock = firstLine;
        this.outOfStock = outOfStockLine;
    }

    public String getInStockLabel(){
        return FtcCore.translateHexCodes(inStock);
    }

    public String getOutOfStockLabel() {
        if(outOfStock != null) return FtcCore.translateHexCodes(inStock);
        return null;
    }
}