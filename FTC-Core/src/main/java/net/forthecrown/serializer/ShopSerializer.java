package net.forthecrown.serializer;

import net.forthecrown.economy.shops.SignShop;

import java.io.File;

public interface ShopSerializer {
    void serialize(SignShop shop);
    void deserialize(SignShop shop);

    File getFile(SignShop shop);
    File getShopDirectory();

    boolean fileExists(SignShop shop);

    void delete(SignShop shop);

    boolean wasDeleted(SignShop shop);
}
