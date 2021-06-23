package net.forthecrown.economy.blackmarket;

import net.forthecrown.economy.blackmarket.merchants.EnchantMerchant;
import net.forthecrown.economy.blackmarket.merchants.MaterialMerchant;
import net.forthecrown.serializer.CrownSerializer;

public interface PirateEconomy extends CrownSerializer {
    void updateDate();

    EnchantMerchant getEnchantMerchant();

    MaterialMerchant getMiningMerchant();

    MaterialMerchant getDropsMerchant();

    MaterialMerchant getCropsMerchant();

    int getMaxEarnings();
}
