package net.forthecrown.economy.blackmarket.merchants;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.economy.blackmarket.DailyEnchantment;
import net.forthecrown.economy.blackmarket.EnchantmentData;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.JsonUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EnchantMerchant implements BlackMarketMerchant {

    private Map<Enchantment, EnchantmentData> data;

    private final List<UUID> boughtEnchant = new ArrayList<>();
    private final List<Enchantment> alreadyPicked = new ArrayList<>();

    private final DailyEnchantment daily;
    public EnchantMerchant() {
        this.daily = new DailyEnchantment();
    }

    @Override
    public Inventory createInventory(CrownUser user) {
        return null;
    }

    @Override
    public void load(JsonElement element) {
        JsonObject json = element.getAsJsonObject();

        daily.deserialize(json.get("daily"));

        data.clear();
        JsonArray array = json.getAsJsonArray("enchants");
        for (JsonElement e: array){
            EnchantmentData eData = new EnchantmentData(e);
            data.put(eData.getEnchantment(), eData);
        }

        boughtEnchant.clear();
        JsonElement alreadyBoughtElement = json.get("alreadyBought");

        if(alreadyBoughtElement != null){
            for (JsonElement e: alreadyBoughtElement.getAsJsonArray()){
                boughtEnchant.add(UUID.fromString(e.getAsString()));
            }
        }

        alreadyPicked.clear();
        JsonElement pickedElement = json.get("alreadyPicked");

        if(pickedElement != null){
            for (JsonElement e: pickedElement.getAsJsonArray()){
                alreadyPicked.add(Enchantment.getByKey(NamespacedKey.fromString(e.getAsString())));
            }
        }
    }

    @Override
    public void update(CrownRandom random, byte day) {
        boughtEnchant.clear();

        EnchantmentData dailyData = random.pickRandomEntry(data.values());

        if(!alreadyPicked.isEmpty()){
            int safeGuard = 300;
            while(alreadyPicked.contains(dailyData.getEnchantment())){
                dailyData = random.pickRandomEntry(data.values());

                safeGuard--;
                if(safeGuard < 0) break;
            }
        }

        daily.update(dailyData, random);

        if(day == 1 || day == 0) alreadyPicked.clear();
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();

        json.add("daily", daily.serialize());
        json.add("enchants", JsonUtils.serializeCollection(data.values(), EnchantmentData::serialize));

        if(!boughtEnchant.isEmpty()) json.add("alreadyBought", JsonUtils.serializeCollection(boughtEnchant, id -> new JsonPrimitive(id.toString())));
        if(!alreadyPicked.isEmpty()) json.add("alreadyPicked", JsonUtils.serializeCollection(alreadyPicked, ench -> new JsonPrimitive(ench.getKey().asString())));

        return json;
    }
}
