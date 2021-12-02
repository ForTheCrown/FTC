package net.forthecrown.core.transformers;

import com.google.gson.JsonObject;
import net.forthecrown.core.Crown;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.serializer.ShopJsonSerializer;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.LocationFileName;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class Shops_YamlToJson {
    public static void checkAndRun() {
        File oldDir = new File(Crown.dataFolder().getPath() + File.separator + "shopdata");
        if(!oldDir.exists() || !oldDir.isDirectory()) return;

        Logger logger = Crown.logger();
        logger.info("Running Shops_YamlToJson");

        new Shops_YamlToJson(oldDir, Crown.logger()).run();

        if(oldDir.delete()) logger.info("Deleted old directory");
        else logger.warning("Failed to delete old directory");
    }

    private final File oldDir;
    private final File newDir;
    private final Logger logger;

    Shops_YamlToJson(File oldDir, Logger logger) {
        this.oldDir = oldDir;
        this.logger = logger;
        this.newDir = ShopJsonSerializer.SHOP_DIR;
    }

    void run() {
        File[] files = oldDir.listFiles();

        for (File f: files) {
            if(f.length() == 0) {
                Crown.logger().info("Found empty shop file at " + f.getName());
                continue;
            }

            YamlShop shop = new YamlShop(f);
            logger.info("Converting " + shop.name);

            File newFile = new File(newDir, shop.name + ".json");

            try {
                newFile.createNewFile();

                JsonUtils.writeFile(shop.serialize(), newFile);
                logger.info("Converted " + shop.name);

                f.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class YamlShop implements JsonSerializable {
        private final YamlConfiguration yaml;
        private final LocationFileName name;

        YamlShop(File file) {
            yaml = YamlConfiguration.loadConfiguration(file);
            name = LocationFileName.parse(file.getName());
        }

        @Override
        public JsonObject serialize() {
            JsonWrapper json = JsonWrapper.empty();

            json.addUUID("ownership", UUID.fromString(yaml.getString("Owner")));
            json.add("price", yaml.getInt("Price"));
            json.add("type", yaml.getString("Type").toLowerCase().replaceAll("_shop", ""));

            JsonWrapper inv = JsonWrapper.empty();
            inv.addItem("exampleItem", yaml.getItemStack("ExampleItem"));

            List<ItemStack> items = (List<ItemStack>) yaml.getList("ItemList");
            inv.addList("items", items, JsonUtils::writeItem);

            json.add("inventory", inv);

            return json.getSource();
        }
    }
}
