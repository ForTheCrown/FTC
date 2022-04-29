package net.forthecrown.economy.houses.components;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.core.Keys;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.kyori.adventure.key.Key;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class EnchanterComponent implements HouseComponent {
    public static final Key KEY = Keys.forthecrown("enchantment_librarian");

    private final Map<Enchantment, EnchantmentData> data = new Object2ObjectOpenHashMap<>();
    private final Set<Enchantment> alreadyChosen = new ObjectOpenHashSet<>();

    private final Map<UUID, CustomerData> customerData = new Object2ObjectOpenHashMap<>();
    private Enchantment daily;

    @Override
    public void deserialize(JsonElement element) {
        data.clear();
        alreadyChosen.clear();
        customerData.clear();
        daily = null;

        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        if(json.has("enchants")) {
            for (Map.Entry<String, JsonElement> e: json.getObject("enchants").entrySet()) {
                JsonWrapper jData = JsonWrapper.of(e.getValue().getAsJsonObject());
                Enchantment ench = Enchantment.getByKey(Keys.parse(e.getKey()));

                EnchantmentData data = new EnchantmentData(ench);
                data.basePrice = jData.getInt("basePrice");
                data.costPerLevel = jData.getInt("levelCost");

                if(json.has("alreadyChosen")) alreadyChosen.add(ench);

                this.data.put(ench, data);
            }
        }

        if(json.has("daily")) {
            this.daily = Enchantment.getByKey(json.getKey("daily"));
        }
    }

    @Override
    public JsonElement serialize() {
        return null;
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    @Override
    public void onDayChange() {
        customerData.clear();

        if(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1) {
            alreadyChosen.clear();
        }
    }

    public static class EnchantmentData implements JsonSerializable {
        private final Enchantment enchantment;
        private int basePrice, costPerLevel;

        public EnchantmentData(Enchantment enchantment) {
            this.enchantment = enchantment;
        }

        public Enchantment getEnchantment() {
            return enchantment;
        }

        public int getBasePrice() {
            return basePrice;
        }

        public int getCostPerLevel() {
            return costPerLevel;
        }

        public void setBasePrice(int basePrice) {
            this.basePrice = basePrice;
        }

        public void setCostPerLevel(int costPerLevel) {
            this.costPerLevel = costPerLevel;
        }

        @Override
        public JsonElement serialize() {
            return null;
        }
    }

    private static class CustomerData {
        private int boughtAmount;
        private int price;
    }
}