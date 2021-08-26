package net.forthecrown.economy.shops.template;

import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.serializer.CrownSerializer;

public interface ShopTemplateManager extends CrownSerializer {
    void onSetTemplate(SignShop shop, ShopTemplate template);
    void onRemoveTemplate(SignShop shop);

    void onTemplateDelete(ShopTemplate template);

    void onEditTemplate(ShopTemplate template);
}
