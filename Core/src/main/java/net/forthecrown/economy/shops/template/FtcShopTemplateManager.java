package net.forthecrown.economy.shops.template;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.Crown;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;

import java.util.Map;

public class FtcShopTemplateManager extends AbstractJsonSerializer implements ShopTemplateManager {
    private final Map<String, Key> shops2Templates = new Object2ObjectOpenHashMap<>();

    public FtcShopTemplateManager() {
        super("shop_templates");

        reload();
        Crown.logger().info("Shop templates loaded");
    }

    @Override
    protected void save(JsonBuf json) {
        JsonBuf tracker = JsonBuf.empty();

        for (Map.Entry<String, Key> e: shops2Templates.entrySet()) {
            tracker.addKey(e.getKey(), e.getValue());
        }
        json.add("tracker", tracker);

        JsonArray array = new JsonArray();

        for (ShopTemplate t: Registries.SHOP_TEMPLATES) {
            ShopTemplateType type = t.getType();

            JsonBuf templateJson = JsonBuf.empty();

            templateJson.addKey("type", type.key());
            templateJson.addKey("key", t.key());
            templateJson.add("value", type.serialize(t));

            array.add(templateJson.getSource());
        }

        json.add("templates", array);
    }

    @Override
    protected void reload(JsonBuf json) {
        shops2Templates.clear();
        JsonBuf tracker = json.getBuf("tracker");

        for (Map.Entry<String, JsonElement> e: tracker.entrySet()) {
            shops2Templates.put(e.getKey(), JsonUtils.readKey(e.getValue()));
        }

        JsonArray templates = json.getArray("templates");

        Registries.SHOP_TEMPLATES.clear();
        for (JsonElement e: templates) {
            JsonBuf buf = JsonBuf.of(e.getAsJsonObject());

            ShopTemplateType<ShopTemplate> type = Registries.SHOP_TEMPLATE_TYPES.get(buf.getKey("type"));
            ShopTemplate template = type.deserialize(buf.get("value"), buf.getKey("key"));

            Registries.SHOP_TEMPLATES.register(template.key(), template);
        }
    }

    @Override
    public void onSetTemplate(SignShop shop, ShopTemplate template) {
        shops2Templates.put(shop.getName(), template.key());
    }

    @Override
    public void onRemoveTemplate(SignShop shop) {
        shops2Templates.remove(shop.getName());
    }

    @Override
    public void onTemplateDelete(ShopTemplate template) {
        shops2Templates.entrySet().removeIf(e -> {
            if(!e.getValue().equals(template.key())) return false;

            SignShop shop = Crown.getShopManager().getShop(e.getKey());
            shop.setTemplate(null);

            return true;
        });
    }

    @Override
    public void onEditTemplate(ShopTemplate template) {
        for (Map.Entry<String, Key> e: shops2Templates.entrySet()) {
            if(!e.getValue().equals(template.key())) continue;

            SignShop shop = Crown.getShopManager().getShop(e.getKey());
            shop.update();
        }
    }
}
