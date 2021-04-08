package net.forthecrown.core.files;

import com.google.common.base.Charsets;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.BlackMarket;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.DailyEnchantment;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.comvars.ComVar;
import net.forthecrown.core.inventories.CustomInventoryHolder;
import net.forthecrown.core.utils.CrownItems;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.utils.ListUtils;
import net.forthecrown.core.utils.MapUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static net.forthecrown.core.utils.BMUtils.*;

public class CrownBlackMarket implements BlackMarket {

    private YamlConfiguration configFile;
    private final File file;

    private Map<Material, ComVar<Short>> crops = new HashMap<>();
    private Map<Material, ComVar<Short>> mining = new HashMap<>();
    private Map<Material, ComVar<Short>> drops = new HashMap<>();
    private Map<Enchantment, ComVar<Integer>> enchants = new HashMap<>();

    private final Map<Material, Integer> amountEarned = new HashMap<>();
    private Set<UUID> boughtEnchant = new HashSet<>();

    private List<Material> dailyCrops = new ArrayList<>();
    private List<Material> dailyMining = new ArrayList<>();
    private List<Material> dailyDrops = new ArrayList<>();
    private Enchantment enchantment;
    private CrownDailyEnchantment dailyEnchantment;

    private int maxEarnings = 50000;
    private byte dayOfWeek;
    private boolean enchantAvailable = true;

    public CrownBlackMarket(FtcCore core){
        file = new File(core.getDataFolder(), "blackmarket.yml");
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        configFile = YamlConfiguration.loadConfiguration(file);

        final InputStream defConfig = core.getResource("blackmarket.yml");
        if(defConfig == null) return;

        configFile.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfig, Charsets.UTF_8)));
        configFile.options().copyDefaults(true);

        try {
            configFile.save(file);
        } catch (Exception e){
            e.printStackTrace();
        }

        reload();
        //doEnchantTimer(core);
    }

    public void reload(){
        configFile = YamlConfiguration.loadConfiguration(file);

        crops = pricesFromConfig(configFile.getConfigurationSection("Price_Per_Item.Crops"), "crops");
        drops = pricesFromConfig(configFile.getConfigurationSection("Price_Per_Item.MobDrops"), "drops");
        mining = pricesFromConfig(configFile.getConfigurationSection("Price_Per_Item.Mining"), "mining");
        enchants = enchantsFromConfig(configFile.getConfigurationSection("Price_Per_Item.Enchants"));

        dayOfWeek = (byte) configFile.getInt("Day");
        maxEarnings = configFile.getInt("MaxEarnings");

        Set<UUID> temp = new HashSet<>();
        for (String s: configFile.getStringList("PurchasedEnchant")){
            try {
                temp.add(UUID.fromString(s));
            } catch (Exception ignored) {}
        }
        boughtEnchant = temp;


        dailyCrops = ListUtils.convertToList(configFile.getStringList("Daily_Items.Crops"), Material::valueOf);
        dailyDrops = ListUtils.convertToList(configFile.getStringList("Daily_Items.Drops"), Material::valueOf);
        dailyMining = ListUtils.convertToList(configFile.getStringList("Daily_Items.Mining"), Material::valueOf);
        enchantment = EnchantmentWrapper.getByKey(NamespacedKey.minecraft(configFile.getString("Daily_Items.Enchant").toLowerCase()));

        dailyEnchantment = new CrownDailyEnchantment(this, enchantment, enchants.get(enchantment), (byte) (enchantment.getMaxLevel() + CrownUtils.getRandomNumberInRange(1, 2)));

        if(dayOfWeek != Calendar.getInstance(CrownUtils.SERVER_TIME_ZONE).get(Calendar.DAY_OF_WEEK)) randomizeItems();
    }

    private void randomizeItems(){
        dayOfWeek = (byte) Calendar.getInstance(CrownUtils.SERVER_TIME_ZONE).get(Calendar.DAY_OF_WEEK);

        List<Enchantment> tempEnchList = new ArrayList<>(enchants.keySet());
        enchantment = tempEnchList.get(CrownUtils.getRandomNumberInRange(0, tempEnchList.size()-1));

        //whew boi this is retarded lmao
        List<Material> alreadyChosen = new ArrayList<>();

        List<Material> temp = new ArrayList<>(drops.keySet());
        dailyDrops.clear();
        int i = 0;
        while (i != 5){
            for (Material mat : temp){
                if(i == 5) break;
                if(alreadyChosen.contains(mat)) continue;

                int a = CrownUtils.getRandomNumberInRange(0, 1);
                if (a == 1){
                    dailyDrops.add(mat);
                    i++;
                    alreadyChosen.add(mat);
                }
            }
        }
        temp = new ArrayList<>(crops.keySet());
        dailyCrops.clear();
        i=0;
        while (i != 5){
            for (Material mat : temp){
                if(i == 5) break;
                if(alreadyChosen.contains(mat)) continue;

                int b = CrownUtils.getRandomNumberInRange(0, 1);
                if (b == 1){
                    dailyCrops.add(mat);
                    i++;
                    alreadyChosen.add(mat);
                }
            }
        }
        temp = new ArrayList<>(mining.keySet());
        dailyMining.clear();
        i=0;
        while (i != 5){
            for (Material mat : temp){
                if(i == 5) break;
                if(alreadyChosen.contains(mat)) continue;

                int b = CrownUtils.getRandomNumberInRange(0, 1);
                if (b == 1){
                    dailyMining.add(mat);
                    i++;
                    alreadyChosen.add(mat);
                }
            }
        }
        boughtEnchant.clear();
        dailyEnchantment = new CrownDailyEnchantment(this, enchantment, enchants.get(enchantment), (byte) (enchantment.getMaxLevel() + CrownUtils.getRandomNumberInRange(1, 2)));

        save();
    }

    public void save(){
        ConfigurationSection prices = configFile.createSection("Price_Per_Item");
        prices.createSection("Crops", MapUtils.convert(crops, Material::toString, ComVar::getValue));
        prices.createSection("MobDrops", MapUtils.convert(drops, Material::toString, ComVar::getValue));
        prices.createSection("Mining", MapUtils.convert(mining, Material::toString, ComVar::getValue));

        Map<String, Integer> tempMap = new HashMap<>();
        for (Enchantment enchant : enchants.keySet()){
            if(enchant == null) continue;
            tempMap.put(enchant.getKey().toString().replaceAll("minecraft:", ""), enchants.get(enchant).getValue());
        }
        prices.createSection("Enchants", tempMap);

        ConfigurationSection dailyItems = configFile.createSection("Daily_Items");

        dailyItems.set("Crops", ListUtils.convertToList(dailyCrops, Material::toString));
        dailyItems.set("Drops", ListUtils.convertToList(dailyDrops, Material::toString));
        dailyItems.set("Mining",ListUtils.convertToList(dailyMining, Material::toString));
        dailyItems.set("Enchant", getEnchantment().getEnchantment().getKey().toString().replaceAll("minecraft:", ""));

        configFile.set("MaxEarnings", maxEarnings);
        configFile.set("Day", dayOfWeek);
        configFile.set("EnchantLevel", getEnchantment().getLevel());
        configFile.set("EnchantPrice", getEnchantment().getPrice());

        configFile.createSection("AmountEarned", MapUtils.convertKeys(amountEarned, Material::toString));
        configFile.set("PurchasedEnchant", ListUtils.convertToList(boughtEnchant, UUID::toString));

        try {
            configFile.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DailyEnchantment getEnchantment(){
        return dailyEnchantment;
    }

    @Override
    public Integer getAmountEarned(Material material){
        if(amountEarned.keySet().contains(material)) return amountEarned.get(material);
        return 0;
    }

    @Override
    public void setAmountEarned(Material material, Integer amount){
        amountEarned.put(material, amount);
    }

    @Override
    public boolean isSoldOut(Material material){
        if(getAmountEarned(material) != null) return getAmountEarned(material) > maxEarnings;
        else return false;
    }

    @Override
    public Short getItemPrice(Material material){
        if (crops.containsKey(material)) return crops.get(material).getValue();
        if (mining.containsKey(material)) return mining.get(material).getValue();
        if (drops.containsKey(material)) return drops.get(material).getValue();
        return null;
    }

    private void doEnchantTimer(FtcCore core){
        Bukkit.getScheduler().scheduleSyncRepeatingTask(core, () -> enchantAvailable = !enchantAvailable, 216000, 216000);
        System.out.println("Edward is now selling: " + enchantAvailable);
    }

    @Override
    public boolean isAllowedToBuyEnchant(Player player){
        return !boughtEnchant.contains(player.getUniqueId());
    }

    @Override
    public void setAllowedToBuyEnchant(Player p, boolean allowed){
        UUID id = p.getUniqueId();
        UserManager um = FtcCore.getUserManager();
        boolean isAlt = um.isAlt(id);

        if(allowed){
            boughtEnchant.remove(p.getUniqueId());
            boughtEnchant.removeAll(isAlt ? um.getAlts(um.getMain(id)) : um.getAlts(id));
        } else {
            boughtEnchant.add(p.getUniqueId());
            boughtEnchant.addAll(isAlt ? um.getAlts(um.getMain(id)) : um.getAlts(id));
        }
    }

    @Override
    public boolean enchantAvailable(){
        return enchantAvailable;
    }

    @Override
    public void setItemPrice(Material material, Short price){
        if(mining.containsKey(material)) setPrice(mining.get(material), price);
        if(drops.containsKey(material)) setPrice(drops.get(material), price);
        if(crops.containsKey(material)) setPrice(crops.get(material), price);
        throw new IllegalArgumentException(material.toString() + " is not a valid material");
    }

    private void setPrice(ComVar<Short> comVar, short price){
        comVar.setValue(price);
    }

    @Override
    public Integer getEnchantBasePrice(Enchantment enchantment){
        return enchants.get(enchantment).getValue();
    }
    @Override
    public void setEnchantBasePrice(Enchantment enchantment, Integer price){
        enchants.put(enchantment, enchants.get(enchantment).setValue(price));
    }

    @Override
    public Inventory getDropInventory(CrownUser user){
        ItemStack header = CrownItems.makeItem(Material.ROTTEN_FLESH, 1, true, "&bDrops");
        Inventory inv = getBaseInventory("Black Market: Drops", header);

        int i = 11;
        for (Material stack: dailyDrops) {
            inv.setItem(i, makeSellableItem(stack, user));
            i++;
        }

        return inv;
    }

    @Override
    public Inventory getMiningInventory(CrownUser user){
        ItemStack header = CrownItems.makeItem(Material.IRON_PICKAXE, 1, true, "&bMining");
        Inventory inv = getBaseInventory("Black Market: Mining", header);

        int i = 11;
        for (Material stack: dailyMining) {
            inv.setItem(i, makeSellableItem(stack, user));
            i++;
        }

        return inv;
    }

    @Override
    public Inventory getFarmingInventory(CrownUser user){
        ItemStack header = CrownItems.makeItem(Material.OAK_SAPLING, 1, true, "&bCrops");
        Inventory inv = getBaseInventory("Black Market: Crops", header);

        int i = 11;
        for (Material stack: dailyCrops) {
            inv.setItem(i, makeSellableItem(stack, user));
            i++;
        }

        return inv;
    }

    @Override
    public Inventory getEnchantInventory(ItemStack userItem, boolean accepting){
        ItemStack rod = CrownItems.makeItem(Material.END_ROD, 1, true, "&7-");
        rod.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        ItemStack purpleGlass = CrownItems.makeItem(Material.PURPLE_STAINED_GLASS_PANE, 1 ,true, "&7-");

        Inventory inv = getBaseInventory("Black Market: Enchants", rod);

        final ItemStack border = CrownItems.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "&7-");
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
    public ItemStack getAcceptButton(){
        return CrownItems.makeItem(Material.LIME_STAINED_GLASS_PANE, 1, true, ChatColor.GREEN + "[Accept and Pay]",
                "&7Enchant the item for " + CrownUtils.decimalizeNumber(getEnchantment().getPrice()) + " Rhines");
    }

    @Override
    public ItemStack getDenyButton(){
        return CrownItems.makeItem(Material.RED_STAINED_GLASS_PANE, 1, true, ChatColor.RED + "[Cannot accept]",
                "&7Cannot accept enchantment!");
    }

    @Override
    public Inventory getParrotInventory(){
        Inventory invToOpen = new CustomInventoryHolder("Parrot Shop", 27).getInventory();
        ItemStack pane = CrownItems.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, ChatColor.GRAY + " ");

        for (int i = 0; i < 10; i++) {
            invToOpen.setItem(i, pane);
        }
        for (int i = 17; i < 27; i++) {
            invToOpen.setItem(i, pane);
        }

        invToOpen.setItem(11, CrownItems.makeItem(Material.GRAY_WOOL, 1 , true, ChatColor.GRAY + "Gray Parrot", ChatColor.YELLOW + "Value: 50,000 Rhines.", ChatColor.DARK_GRAY + "Do /parrot gray to summon it"));
        invToOpen.setItem(12, CrownItems.makeItem(Material.GREEN_WOOL, 1, true, ChatColor.GREEN + "Green Parrot", ChatColor.YELLOW + "Value: 50,000 Rhines.", ChatColor.DARK_GRAY + "Do /parrot green to summon it"));
        invToOpen.setItem(13, CrownItems.makeItem(Material.BLUE_WOOL, 1, true, ChatColor.BLUE + "Blue Parrot", ChatColor.YELLOW + "Value: 100,000 Rhines.", ChatColor.DARK_GRAY + "Do /parrot blue to summon it"));
        invToOpen.setItem(14, CrownItems.makeItem(Material.RED_WOOL, 1, true, ChatColor.RED + "Red Parrot", ChatColor.YELLOW + "Value: Available only for Captains.", ChatColor.DARK_GRAY + "Do /parrot red to summon it"));
        invToOpen.setItem(15, CrownItems.makeItem(Material.LIGHT_BLUE_WOOL, 1, true, ChatColor.AQUA + "Aqua Parrot", ChatColor.YELLOW + "Value: Available only for Admirals.", ChatColor.DARK_GRAY + "Do /parrot aqua to summon it"));

        return invToOpen;
    }

    private Inventory getBaseInventory(String name, ItemStack header){
        CustomInventoryHolder holder = new CustomInventoryHolder(name, InventoryType.CHEST);
        Inventory base = holder.getInventory();
        final ItemStack borderItem = CrownItems.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "&7-");

        base.setItem(4, header);

        for (int i = 0; i < 27; i++){
            if(i == 4) i++;
            if(i == 10) i += 7;

            base.setItem(i, borderItem);
        }

        return base;
    }

    private ItemStack makeSellableItem(Material material, CrownUser user){
        String s = material.toString().toLowerCase().replaceAll("_", " ");
        ItemStack item = CrownItems.makeItem(material, 1, true, s,
                "&eValue: " + getItemPrice(material) + " Rhines per item,",
                "&6" + getItemPrice(material) * 64 + " Rhines for a stack.",
                "&7Amount of items you will sell: " + user.getSellAmount().toString().toLowerCase().replaceAll("_", " "));

        if(isSoldOut(material)){
            item = CrownItems.makeItem(material, 1, true, ChatColor.GRAY + s,
                    "&7No longer available!");
        }

        return item;
    }

    private ItemStack getCoolEnchant(){
        ItemStack item = CrownItems.makeItem(Material.ENCHANTED_BOOK, 1, false, null, "&6Value: &e" + CrownUtils.decimalizeNumber(getEnchantment().getPrice()) + " Rhines&6.");
        item.addUnsafeEnchantment(getEnchantment().getEnchantment(), getEnchantment().getLevel());

        return item;
    }
}
