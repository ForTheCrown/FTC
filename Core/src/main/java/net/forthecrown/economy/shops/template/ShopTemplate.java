package net.forthecrown.economy.shops.template;

import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.economy.shops.SignShopSession;
import net.kyori.adventure.key.Keyed;
import org.bukkit.block.Sign;

public interface ShopTemplate extends Keyed {
    void onApply(SignShop shop);
    void onSignUpdate(SignShop shop, Sign sign);

    void onShopUse(SignShopSession session);

    ShopTemplateType<? extends ShopTemplate> getType();
}
