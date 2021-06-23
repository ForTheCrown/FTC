package net.forthecrown.economy.blackmarket;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.serializer.JsonSerializable;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

public class EnchantmentData implements JsonSerializable {
    private final Enchantment enchantment;
    private byte maxLevel;
    private int pricePerLevel;

    public EnchantmentData(JsonElement element){
        JsonObject json = element.getAsJsonObject();

        this.enchantment = Enchantment.getByKey(NamespacedKey.fromString(json.get("enchantment").getAsString()));
        this.maxLevel = json.get("maxLevel").getAsByte();
        this.pricePerLevel = json.get("pricePerLevel").getAsInt();
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public byte getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(byte maxLevel) {
        this.maxLevel = maxLevel;
    }

    public int getPricePerLevel() {
        return pricePerLevel;
    }

    public void setPricePerLevel(int pricePerLevel) {
        this.pricePerLevel = pricePerLevel;
    }

    @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();

        json.addProperty("enchantment", enchantment.getKey().asString());
        json.addProperty("maxLevel", maxLevel);
        json.addProperty("pricePerLevel", pricePerLevel);

        return json;
    }
}
