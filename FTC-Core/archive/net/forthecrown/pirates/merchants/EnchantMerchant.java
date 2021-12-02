package net.forthecrown.economy.pirates.merchants;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.clickevent.ClickEventManager;
import net.forthecrown.commands.clickevent.ClickEventTask;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.pirates.DailyEnchantment;
import net.forthecrown.economy.pirates.EnchantmentData;
import net.forthecrown.events.dynamic.BmEnchantListener;
import net.forthecrown.inventory.FtcItems;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.squire.Squire;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static net.forthecrown.economy.pirates.BlackMarketUtils.getBaseInventory;

public class EnchantMerchant implements BlackMarketMerchant, ClickEventTask {
    public static final Key KEY = Squire.createPiratesKey("enchants");

    private final Map<Enchantment, EnchantmentData> data = new HashMap<>();

    private final List<UUID> boughtEnchant = new ArrayList<>();
    private final List<Enchantment> alreadyPicked = new ArrayList<>();
    private final String npcID;

    private final DailyEnchantment daily;
    public EnchantMerchant() {
        daily = new DailyEnchantment();

        npcID = ClickEventManager.registerClickEvent(this);
    }

    @Override
    public Inventory createInventory(CrownUser user) {
        return createInventory(user, true, null);
    }

    public Inventory createInventory(CrownUser user, boolean accepting, ItemStack userItem){
        final ItemStack rod = FtcItems.makeItem(Material.END_ROD, 1, true, "&7-");
        rod.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        final ItemStack purpleGlass = FtcItems.makeItem(Material.PURPLE_STAINED_GLASS_PANE, 1 ,true, "&7-");
        final ItemStack border = FtcItems.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "&7-");

        Inventory inv = getBaseInventory("Black Market: Enchants", rod);

        inv.setItem(10, border);
        inv.setItem(16, border);

        inv.setItem(12, rod);
        inv.setItem(14, rod);
        inv.setItem(22, rod);

        inv.setItem(0, purpleGlass);
        inv.setItem(8, purpleGlass);
        inv.setItem(18, purpleGlass);
        inv.setItem(26, purpleGlass);

        ItemStack acceptingOrDenying = getAcceptButton();
        if(!accepting) acceptingOrDenying = getDenyButton();
        inv.setItem(13, acceptingOrDenying);

        if(userItem != null) inv.setItem(11, userItem);

        inv.setItem(15, getCoolEnchant());

        return inv;
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
        Crown.logger().info("Picking " + dailyData.getEnchantment().getKey().toString() + " as the daily enchantment");

        if(day == 1 || day == 0) alreadyPicked.clear();

        alreadyPicked.add(dailyData.getEnchantment());
    }

    public ItemStack getAcceptButton(){
        return FtcItems.makeItem(Material.LIME_STAINED_GLASS_PANE, 1, true, ChatColor.GREEN + "[Accept and Pay]",
                "&7Enchant the item for " + FtcFormatter.decimalizeNumber(daily.getPrice()) + " Rhines");
    }

    public ItemStack getDenyButton(){
        return FtcItems.makeItem(Material.RED_STAINED_GLASS_PANE, 1, true, ChatColor.RED + "[Cannot accept]",
                "&7Cannot accept enchantment!");
    }

    private ItemStack getCoolEnchant(){
        ItemStack item = FtcItems.makeItem(Material.ENCHANTED_BOOK, 1, false, null,
                Component.text("Value: ")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .append(FtcFormatter.rhines(daily.getPrice())
                                .color(NamedTextColor.YELLOW)
                        )
                        .append(Component.text("."))
                        .color(NamedTextColor.GOLD));
        item.addUnsafeEnchantment(daily.getEnchantment(), daily.getLevel());

        return item;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();

        json.add("daily", daily.serialize());
        json.add("enchants", JsonUtils.writeCollection(data.values(), EnchantmentData::serialize));

        if(!boughtEnchant.isEmpty()) json.add("alreadyBought", JsonUtils.writeCollection(boughtEnchant, id -> new JsonPrimitive(id.toString())));
        if(!alreadyPicked.isEmpty()) json.add("alreadyPicked", JsonUtils.writeCollection(alreadyPicked, ench -> new JsonPrimitive(ench.getKey().asString())));

        return json;
    }

    @Override
    public void onUse(CrownUser user, Entity edward) {
        user.sendMessage(
                Component.translatable("pirates.enchants.selling",
                        NamedTextColor.GRAY,
                        Component.text("Edward")
                                .hoverEvent(edward)
                                .color(NamedTextColor.YELLOW),
                        daily.getEnchantment().displayName(daily.getLevel()).color(NamedTextColor.YELLOW),
                        FtcFormatter.rhines(daily.getPrice()).color(NamedTextColor.GOLD)
                )
        );

        ClickEventManager.allowCommandUsage(user.getPlayer(), true);

        Component text = Component.translatable("pirates.enchants.button").color(NamedTextColor.AQUA)
                .clickEvent(ClickEventManager.getClickEvent(npcID))
                .hoverEvent(HoverEvent.showText(Component.translatable("pirates.enchants.button.hover")));

        Component text1 = Component.translatable("pirates.enchants.proposal")
                .color(NamedTextColor.GRAY)
                .append(Component.space())
                .append(text);

        user.sendMessage(text1);
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    public DailyEnchantment getDaily() {
        return daily;
    }

    public boolean isAllowedToBuy(UUID id){
        return !boughtEnchant.contains(id);
    }

    public void setAllowedToBuy(UUID id, boolean allowed){
        if(allowed) boughtEnchant.remove(id);
        else boughtEnchant.add(id);
    }

    @Override
    public void run(Player player, String[] args) throws CommandSyntaxException {
        if(Pirates.getPirateEconomy().getEnchantMerchant().isAllowedToBuy(player.getUniqueId())) {
            player.openInventory(createInventory(UserManager.getUser(player)));

            Bukkit.getPluginManager().registerEvents(new BmEnchantListener(player), Crown.inst());
        } else {
            throw FtcExceptionProvider.translatable("pirates.enchants.alreadyBought", NamedTextColor.YELLOW);
        }
    }
}
