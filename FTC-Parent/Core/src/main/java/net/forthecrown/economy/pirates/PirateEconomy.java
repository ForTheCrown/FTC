package net.forthecrown.economy.pirates;

import net.forthecrown.economy.pirates.merchants.*;
import net.forthecrown.serializer.CrownSerializer;

public interface PirateEconomy extends CrownSerializer {
    boolean shouldUpdateDate();

    void updateDate();

    EnchantMerchant getEnchantMerchant();

    MaterialMerchant getMiningMerchant();

    MaterialMerchant getDropsMerchant();

    MaterialMerchant getCropsMerchant();

    HeadMerchant getHeadMerchant();

    ParrotMerchant getParrotMerchant();

    int getMaxEarnings();

    void setMaxEarnings(int maxEarnings);

    UsablePirateNpc getNpcById(String id);
}
