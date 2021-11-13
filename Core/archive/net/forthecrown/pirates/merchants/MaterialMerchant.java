package net.forthecrown.economy.pirates.merchants;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.pirates.BlackMarketUtils;
import net.forthecrown.events.dynamic.BmSellItemListener;
import net.forthecrown.inventory.FtcItems;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.squire.Squire;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialMerchant implements BlackMarketMerchant {
    public static final Key DROPS_KEY = Squire.createPiratesKey("drops");
    public static final Key CROPS_KEY = Squire.createPiratesKey("crops");
    public static final Key MINING_KEY = Squire.createPiratesKey("mining");

    public static final ItemStack DROPS_HEADER = makeHeaderItem(Material.ROTTEN_FLESH, "Drops");
    public static final ItemStack CROPS_HEADER = makeHeaderItem(Material.OAK_SAPLING, "Crops");
    public static final ItemStack MINING_HEADER = makeHeaderItem(Material.IRON_PICKAXE, "Mining");

    private final Map<Material, Short> prices = new HashMap<>();
    private final Map<Material, Integer> earned = new HashMap<>();
    private List<Material> chosenItems = new ArrayList<>();

    private final String name;

    public MaterialMerchant(String name) {
        this.name = name;
    }

    @Override
    public Inventory createInventory(CrownUser user) {
        Inventory inv = BlackMarketUtils.getBaseInventory("Black market: " + name, header());

        int i = 11;
        for (Material stack: chosenItems) {
            inv.setItem(i, makeSellableItem(stack, user));
            i++;
        }

        return inv;
    }

    @Override
    public void load(JsonElement element) {
        JsonObject json = element.getAsJsonObject();
        JsonObject pricesJson = json.getAsJsonObject("prices");

        prices.clear();
        for (Map.Entry<String, JsonElement> e: pricesJson.entrySet()){
            prices.put(Material.valueOf(e.getKey().toUpperCase()), e.getValue().getAsShort());
        }

        chosenItems.clear();
        for (Map.Entry<String, JsonElement> e: json.getAsJsonObject("chosen").entrySet()){
            Material mat = Material.valueOf(e.getKey().toUpperCase());
            chosenItems.add(mat);

            int earned = e.getValue().getAsInt();
            if(earned > 0) this.earned.put(mat, earned);
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

        JsonObject array = new JsonObject();
        chosenItems.forEach(m -> array.addProperty(m.name().toLowerCase(), earned.getOrDefault(m, 0)));
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
        return getEarned(material) >= Pirates.getPirateEconomy().getMaxEarnings();
    }

    protected ItemStack makeSellableItem(Material material, CrownUser user){
        Component s = Component.translatable(material.getTranslationKey()).color(isSoldOut(material) ? NamedTextColor.GRAY : NamedTextColor.WHITE);
        ItemStack item;

        if(isSoldOut(material)){
            item = FtcItems.makeItem(material, 1, true, s.decoration(TextDecoration.ITALIC, false),
                    Component.text("No longer available!").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        } else {
            item = FtcItems.makeItem(material, 1, true, s.decoration(TextDecoration.ITALIC, false),
                    Component.text("Value: " + getItemPrice(material) + " Rhines per item,").color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false),
                    Component.text(getItemPrice(material) * 64 + " Rhines for a stack.").color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false),
                    Component.text("Amount of items you will sell: " + FtcFormatter.normalEnum(user.getSellAmount())).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        }

        return item;
    }

    protected ItemStack header(){
        return switch (name){
            case "Drops" -> DROPS_HEADER;
            case "Crops" -> CROPS_HEADER;
            default -> MINING_HEADER;
        };
    }

    private static ItemStack makeHeaderItem(Material material, String name){
        return new ItemStackBuilder(material)
                .setName(Component.text(name).style(Style.style(NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)))
                .build();
    }

    @Override
    public void onUse(CrownUser user, Entity entity) {
        BmSellItemListener listener = new BmSellItemListener(user.getPlayer(), this);
        Bukkit.getPluginManager().registerEvents(listener, Crown.inst());

        user.getPlayer().openInventory(createInventory(user));
    }

    @Override
    public @NotNull Key key() {
        return switch (name){
            case "Drops" -> DROPS_KEY;
            case "Crops" -> CROPS_KEY;
            default -> MINING_KEY;
        };
    }
}
