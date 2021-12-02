package net.forthecrown.serializer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.core.Crown;
import net.forthecrown.economy.shops.ShopInventory;
import net.forthecrown.economy.shops.ShopType;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.inventory.FtcItems;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.LocationFileName;
import net.minecraft.Util;
import org.apache.commons.lang3.Validate;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class ShopJsonSerializer implements ShopSerializer {
    public static final File SHOP_DIR = Util.make(() -> {
        File f =  new File(Crown.dataFolder().getPath() + File.separator + "shops");
        if(!f.isDirectory()) f.delete();
        if(!f.exists()) f.mkdir();

        return f;
    });

    public static final Map<LocationFileName, File> NAME_2_SHOPFILE = new Object2ObjectOpenHashMap<>();
    public static final Set<LocationFileName> DELETED = new ObjectOpenHashSet<>();

    @Override
    public void serialize(SignShop shop) {
        JsonWrapper json = JsonWrapper.empty();

        json.add("ownership", shop.getOwnership());
        json.add("price", shop.getPrice());
        json.addEnum("type", shop.getType());

        JsonWrapper inv = JsonWrapper.empty();
        ShopInventory sInv = shop.getInventory();
        if(sInv.getExampleItem() != null) inv.addItem("exampleItem", sInv.getExampleItem());
        if(!sInv.isEmpty()) inv.addList("items", sInv.getShopContents(), JsonUtils::writeItem);
        json.add("inventory", inv);

        writeShop(json, shop);
    }

    @Override
    public void deserialize(SignShop shop) {
        JsonWrapper json = readShop(shop);

        shop.getOwnership().deserialize(json.get("ownership"));
        shop.setPrice(json.getInt("price"), false);
        shop.setType(json.getEnum("type", ShopType.class));

        JsonWrapper inv = json.getWrapped("inventory");
        ShopInventory sInv = shop.getInventory();

        ItemStack item = inv.getItem("exampleItem");

        if(FtcItems.isEmpty(item)) {
            Crown.logger().warning("Found null exampleItem in " + shop.getFileName() + "'s file, bad touch lol");
        }

        sInv.setExampleItem(item);
        sInv.setShopContents(inv.getList("items", JsonUtils::readItem, new ObjectArrayList<>()));
    }

    @Override
    public File getFile(SignShop shop) {
        return NAME_2_SHOPFILE.computeIfAbsent(shop.getFileName(), fileName -> new File(SHOP_DIR, fileName.toString(".json")));
    }

    @Override
    public File getShopDirectory() {
        return SHOP_DIR;
    }

    @Override
    public boolean fileExists(SignShop shop) {
        return getFile(shop).exists();
    }

    @Override
    public void delete(SignShop shop) {
        LocationFileName name = shop.getFileName();
        File f = getFile(shop);

        f.delete();
        NAME_2_SHOPFILE.remove(name);
        DELETED.add(name);
    }

    @Override
    public boolean wasDeleted(SignShop shop) {
        return DELETED.contains(shop.getFileName());
    }

    private void writeShop(JsonWrapper json, SignShop shop) {
        File f = getFile(shop);

        try {
            if(!f.exists()) {
                f.createNewFile();
            }

            JsonUtils.writeFile(json.getSource(), f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JsonWrapper readShop(SignShop shop) {
        try {
            File f = getFile(shop);
            Validate.isTrue(f.exists(), shop.getFileName() + " has no file");

            return JsonWrapper.of(JsonUtils.readFile(f));
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't read shop file for " + shop.getFileName(), e);
        }
    }
}
