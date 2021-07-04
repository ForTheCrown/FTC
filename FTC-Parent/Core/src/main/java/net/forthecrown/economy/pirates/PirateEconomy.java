package net.forthecrown.economy.pirates;

import net.forthecrown.economy.pirates.merchants.*;
import net.forthecrown.registry.Registry;
import net.forthecrown.serializer.CrownSerializer;

public interface PirateEconomy extends CrownSerializer {
    void updateDate();

    EnchantMerchant getEnchantMerchant();

    MaterialMerchant getMiningMerchant();

    MaterialMerchant getDropsMerchant();

    MaterialMerchant getCropsMerchant();

    HeadMerchant getHeadMerchant();

    ParrotMerchant getParrotMerchant();

    GrapplingHookMerchant getGhMerchant();

    int getMaxEarnings();

    void setMaxEarnings(int maxEarnings);

    UsablePirateNpc getNpcById(String id);

    Registry<UsablePirateNpc> getNpcRegistry();
}
