package net.forthecrown.economy.blackmarket.merchants;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.core.inventory.CrownItems;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.CrownRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MaterialMerchant implements BlackMarketMerchant {

    protected final Map<Material, Short> prices = new HashMap<>();
    protected final Map<Material, Integer> earned = new HashMap<>();
    protected List<Material> chosenItems = new ArrayList<>();

    @Override
    public void load(JsonElement element) {
        JsonObject json = element.getAsJsonObject();
        JsonObject pricesJson = json.getAsJsonObject("prices");

        prices.clear();
        for (Map.Entry<String, JsonElement> e: pricesJson.entrySet()){
            prices.put(Material.valueOf(e.getKey().toUpperCase()), e.getValue().getAsShort());
        }

        chosenItems.clear();
        for (JsonElement e: json.getAsJsonArray("chosen")){
            chosenItems.add(Material.valueOf(e.getAsString().toUpperCase()));
        }
    }

    @Override
    public void update(CrownRandom random, byte day) {
        chosenItems.clear();
        chosenItems = random.pickRandomEntries(prices.keySet(), 5);
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        JsonObject priceJson = new JsonObject();

        prices.forEach((m, p) -> priceJson.add(m.name().toLowerCase(), new JsonPrimitive(p)));
        json.add("prices", priceJson);

        JsonArray array = new JsonArray();
        chosenItems.forEach(m -> array.add(m.name().toLowerCase()));
        json.add("chosen", array);

        return json;
    }

    public short getItemPrice(Material material){
        return prices.get(material);
    }

    public void setItemPrice(Material material, short price){
        prices.put(material, price);
    }

    public int getEarned(Material material){
        return earned.getOrDefault(material, 0);
    }

    public void setEarned(Material material, int amount){
        earned.put(material, amount);
    }

    public boolean isSoldOut(Material material){
        return getEarned(material) >= CrownCore.getPirateEconomy().getMaxEarnings();
    }

    protected ItemStack makeSellableItem(Material material, CrownUser user){
        Component s = Component.translatable(material.getTranslationKey()).color(isSoldOut(material) ? NamedTextColor.GRAY : NamedTextColor.WHITE);
        ItemStack item;

        if(isSoldOut(material)){
            item = CrownItems.makeItem(material, 1, true, s.decoration(TextDecoration.ITALIC, false),
                    Component.text("No longer available!").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        } else {
            item = CrownItems.makeItem(material, 1, true, s.decoration(TextDecoration.ITALIC, false),
                    Component.text("Value: " + getItemPrice(material) + " Rhines per item,").color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false),
                    Component.text(getItemPrice(material) * 64 + " Rhines for a stack.").color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false),
                    Component.text("Amount of items you will sell: " + ChatFormatter.normalEnum(user.getSellAmount())).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        }

        return item;
    }
}
