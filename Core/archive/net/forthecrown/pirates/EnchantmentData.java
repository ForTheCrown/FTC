package net.forthecrown.economy.pirates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.Struct;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.util.Objects;

public class EnchantmentData implements JsonSerializable, Struct {
    private final Enchantment enchantment;
    private byte maxLevel;
    private int pricePerLevel;

    public EnchantmentData(JsonElement element) {
        JsonObject json = element.getAsJsonObject();

        String str = json.get("enchantment").getAsString();
        enchantment = Objects.requireNonNull(
                Enchantment.getByKey(
                        NamespacedKey.fromString(str)
                )
        );

        maxLevel = json.get("maxLevel").getAsByte();
        pricePerLevel = json.get("pricePerLevel").getAsInt();
    }

    public EnchantmentData(Enchantment enchantment, byte maxLevel, int pricePerLevel) {
        this.enchantment = enchantment;
        this.maxLevel = maxLevel;
        this.pricePerLevel = pricePerLevel;
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
