package net.forthecrown.core.enums;

import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.ComponentUtils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public enum ShopType {

    SELL_SHOP ("&a&l=[Sell]=", "&4&l=[Sell]="),
    BUY_SHOP ("&a&l=[Buy]=", "&4&l=[Buy]="),
    ADMIN_SELL_SHOP ("&b&l=[Sell]=", "&4&l=[Sell]="),
    ADMIN_BUY_SHOP ("&b&l=[Buy]=", "&4&l=[Buy]=");

    private final String inStock;
    private final String outStock;

    ShopType(@NotNull String inStock, @NotNull String outStock){
        this.inStock = inStock;
        this.outStock = outStock;
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
    public Component getInComponent(){
        return ComponentUtils.convertString(getInStockLabel());
    }

    @NotNull
    public Component getOutComponent(){
        return ComponentUtils.convertString(getOutOfStockLabel());
    }
}