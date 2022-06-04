package net.forthecrown.core.transformers;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.Crown;
import net.forthecrown.economy.shops.*;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.utils.LocationFileName;
import org.apache.logging.log4j.Logger;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class ShopJsonToTag {
    private static final File SHOP_DIR = new File(Crown.dataFolder().getPath() + File.separator + "shops");
    private static final Logger LOGGER = Crown.logger();

    public static void run(FtcShopManager manager) {
        LOGGER.info("Starting shop data transformer");

        for (File f: SHOP_DIR.listFiles()) {
            LOGGER.info("Attempting to transform file: {}", f.getName());
            LocationFileName name = LocationFileName.parse(f.getName());

            try {
                JsonWrapper json = JsonWrapper.of(JsonUtils.readFileObject(f));
                JsonShop shop = new JsonShop(name);
                shop.load(json);

                FtcSignShop signShop = shop.toNormal();

                if (!signShop.getPosition().stateIs(Sign.class)) {
                    LOGGER.warn("Found shop with non sign block at: {}, skipping", name);

                    f.delete();
                    continue;
                }

                Sign sign = signShop.getSign();

                sign.getPersistentDataContainer().remove(ShopConstants.SHOP_KEY);
                sign.getPersistentDataContainer().remove(ShopConstants.LEGACY_SHOP_KEY);

                signShop.save(sign);
                sign.update();

                manager.getAllShopNames().add(name);

                //f.delete();

                LOGGER.info("Converted shop: {}", name);
            } catch (IOException e) {
                LOGGER.error("Error attempting to convert shop: " + name, e);
            }
        }

        LOGGER.info("Finished shop transformer");
        Transformers.complete(ShopJsonToTag.class);
    }

    @RequiredArgsConstructor
    private static class JsonShop {
        private final LocationFileName fileName;

        private ShopType type;
        private int price;
        private UUID owner;

        private ItemStack exampleItem;
        private List<ItemStack> inventory;

        private HistoryEntry[] entries;

        void load(JsonWrapper json) {
            type = json.getEnum("type", ShopType.class);
            price = json.getInt("price");
            owner = json.getUUID("ownership");

            if (json.has("history")) {
                entries = json.getArray("history", HistoryEntry::ofLegacyJson, HistoryEntry[]::new);
            }

            JsonWrapper inv = json.getWrapped("inventory");
            exampleItem = inv.getItem("exampleItem");

            if (!inv.missingOrNull("items")) {
                inventory = new ObjectArrayList<>(inv.getList("items", JsonUtils::readItem));
            }
        }

        FtcSignShop toNormal() {
            FtcSignShop shop = new FtcSignShop(fileName.toVector(), true);

            shop.type = type;
            shop.price = price;

            shop.getInventory().setExampleItem(exampleItem);

            if (!ListUtils.isNullOrEmpty(inventory)) {
                shop.getInventory().setShopContents(inventory);
            }

            shop.getOwnership().setOwner(owner);

            if (entries != null) {
                for (int i = entries.length - 1; i >= 0; i--) {
                    shop.getHistory().addEntry(entries[i]);
                }
            }

            return shop;
        }
    }
}