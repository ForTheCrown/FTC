package net.forthecrown.economy.pirates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.CrownRandom;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

public class DailyEnchantment implements JsonSerializable {

    private Enchantment enchantment;
    private byte level;
    private int price;

    public void deserialize(JsonElement element) {
        JsonObject json = element.getAsJsonObject();

        enchantment = Enchantment.getByKey(NamespacedKey.fromString(json.get("enchantment").getAsString()));
        level = json.get("level").getAsByte();
        price = json.get("price").getAsInt();
    }

    public void update(EnchantmentData data, CrownRandom random){
        enchantment = data.getEnchantment();
        level = (byte) random.intInRange(enchantment.getMaxLevel()+1, data.getMaxLevel());
        price = level * data.getPricePerLevel();
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public void setEnchantment(Enchantment enchantment) {
        this.enchantment = enchantment;
    }

    public byte getLevel() {
        return level;
    }

    public void setLevel(byte level) {
        this.level = level;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();

        json.addProperty("enchantment", enchantment.getKey().asString());
        json.addProperty("level", level);
        json.addProperty("price", price);

        return json;
    }
}
