package net.forthecrown.economy.pirates;

import net.forthecrown.economy.pirates.merchants.*;
import net.forthecrown.serializer.CrownSerializer;

/**
 * Represents the pirate's BM economy.
 * <p></p>
 * Implementation: {@link FtcPirateEconomy}
 */
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
}
