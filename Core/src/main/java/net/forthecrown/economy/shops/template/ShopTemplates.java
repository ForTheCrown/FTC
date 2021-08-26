package net.forthecrown.economy.shops.template;

import net.forthecrown.registry.Registries;

public class ShopTemplates {
    public static GenericShopTemplateType ADMIN_SHOP;

    public static void init() {
        ADMIN_SHOP = register(new GenericShopTemplateType());
    }

    private static <T extends ShopTemplateType<?>> T register(ShopTemplateType val) {
        return (T) Registries.SHOP_TEMPLATE_TYPES.register(val.key(), val);
    }
}
