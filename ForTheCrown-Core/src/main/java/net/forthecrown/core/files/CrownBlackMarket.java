package net.forthecrown.core.files;

import com.google.common.base.Charsets;
import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.BlackMarket;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.DailyEnchantment;
import net.forthecrown.core.inventories.CustomInventoryHolder;
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

public class CrownBlackMarket implements BlackMarket {

    private YamlConfiguration configFile;
    private final File file;

    private final Map<Material, Integer> crops = new HashMap<>();
    private final Map<Material, Integer> mining = new HashMap<>();
    private final Map<Material, Integer> drops = new HashMap<>();
    private final Map<Enchantment, Integer> enchants = new HashMap<>();

    private final Map<Material, Integer> amountEarned = new HashMap<>();
    private Set<UUID> boughtEnchant = new HashSet<>();

    private List<Material> dailyCrops = new ArrayList<>();
    private List<Material> dailyMining = new ArrayList<>();
    private List<Material> dailyDrops = new ArrayList<>();
    private Enchantment dailyEnchantment;
    private CrownDailyEnchantment enchantmentType;

    private int maxEarnings = 50000;
    private int dayOfWeek;
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
        doEnchantTimer(core);
    }

    public void reload(){
        configFile = YamlConfiguration.loadConfiguration(file);

        crops.clear();
        for (String s : configFile.getConfigurationSection("Price_Per_Item.Crops").getKeys(false)){
            crops.put(Material.valueOf(s), configFile.getConfigurationSection("Price_Per_Item.Crops").getInt(s));
        }

        drops.clear();
        for (String s : configFile.getConfigurationSection("Price_Per_Item.MobDrops").getKeys(false)){
            drops.put(Material.valueOf(s), configFile.getConfigurationSection("Price_Per_Item.MobDrops").getInt(s));
        }

        mining.clear();
        for (String s : configFile.getConfigurationSection("Price_Per_Item.Mining").getKeys(false)){
            mining.put(Material.valueOf(s), configFile.getConfigurationSection("Price_Per_Item.Mining").getInt(s));
        }

        enchants.clear();
        for (String s : configFile.getConfigurationSection("Price_Per_Item.Enchants").getKeys(false)){
            enchants.put(EnchantmentWrapper.getByKey(NamespacedKey.minecraft(s.toLowerCase())), configFile.getConfigurationSection("Price_Per_Item.Enchants").getInt(s));
        }

        dayOfWeek = configFile.getInt("Day");
        maxEarnings = configFile.getInt("MaxEarnings");

        Set<UUID> temp = new HashSet<>();
        for (String s: configFile.getStringList("PurchasedEnchant")){
            try {
                temp.add(UUID.fromString(s));
            } catch (Exception ignored) {}
        }
        boughtEnchant = temp;


        dailyCrops = convertStringToMaterial(configFile.getStringList("Daily_Items.Crops"));
        dailyDrops = convertStringToMaterial(configFile.getStringList("Daily_Items.Drops"));
        dailyMining = convertStringToMaterial(configFile.getStringList("Daily_Items.Mining"));
        dailyEnchantment = EnchantmentWrapper.getByKey(NamespacedKey.minecraft(configFile.getString("Daily_Items.Enchant").toLowerCase()));

        enchantmentType = new CrownDailyEnchantment(this, dailyEnchantment, getEnchantBasePrice(dailyEnchantment), dailyEnchantment.getMaxLevel() + CrownUtils.getRandomNumberInRange(1, 2));

        if(dayOfWeek != Calendar.getInstance(CrownUtils.SERVER_TIME_ZONE).get(Calendar.DAY_OF_WEEK)) randomizeItems();
    }

    private void randomizeItems(){
        dayOfWeek = Calendar.getInstance(CrownUtils.SERVER_TIME_ZONE).get(Calendar.DAY_OF_WEEK);

        List<Enchantment> tempEnchList = new ArrayList<>(enchants.keySet());
        dailyEnchantment = tempEnchList.get(CrownUtils.getRandomNumberInRange(0, tempEnchList.size()-1));

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
        enchantmentType = new CrownDailyEnchantment(this, dailyEnchantment, getEnchantBasePrice(dailyEnchantment), dailyEnchantment.getMaxLevel() + CrownUtils.getRandomNumberInRange(1, 2));

        save();
    }

    public void save(){
        ConfigurationSection prices = configFile.createSection("Price_Per_Item");
        prices.createSection("Crops", makeStringMap(crops));
        prices.createSection("MobDrops", makeStringMap(drops));
        prices.createSection("Mining", makeStringMap(mining));

        Map<String, Integer> tempMap = new HashMap<>();
        for (Enchantment enchant : enchants.keySet()){
            if(enchant == null) continue;
            tempMap.put(enchant.getKey().toString().replaceAll("minecraft:", ""), enchants.get(enchant));
        }
        prices.createSection("Enchants", tempMap);

        ConfigurationSection dailyItems = configFile.createSection("Daily_Items");

        dailyItems.set("Crops", makeStringList(dailyCrops));
        dailyItems.set("Drops", makeStringList(dailyDrops));
        dailyItems.set("Mining",makeStringList(dailyMining));
        dailyItems.set("Enchant", getDailyEnchantment().getEnchantment().getKey().toString().replaceAll("minecraft:", ""));

        configFile.set("MaxEarnings", maxEarnings);
        configFile.set("Day", dayOfWeek);
        configFile.set("EnchantLevel", getDailyEnchantment().getLevel());
        configFile.set("EnchantPrice", getDailyEnchantment().getPrice());

        configFile.createSection("AmountEarned", makeStringMap(amountEarned));

        List<String> temp = new ArrayList<>();
        for (UUID id: boughtEnchant){
            temp.add(id.toString());
        }
        configFile.set("PurchasedEnchant", temp);

        try {
            configFile.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Integer> makeStringMap(Map<Material, Integer> map){
        Map<String, Integer> tempMap = new HashMap<>();
        for (Material mat : map.keySet()){
            tempMap.put(mat.toString(), map.get(mat));
        }
        return tempMap;
    }

    private List<String> makeStringList( List<Material> list){
        if(list == null) return null;

        List<String> temp = new ArrayList<>();
        for (Material material : list) {
            temp.add(material.name());
        }
        return temp;
    }

    private List<Material> convertStringToMaterial(List<String> list){
        List<Material> temp = new ArrayList<>();
        for (String s : list){
            Material mat;
            try {
                mat = Material.valueOf(s);
            } catch (Exception e) { throw new NullPointerException("List conversion failure"); }

            temp.add(mat);
        }
        return temp;
    }

    @Override
    public DailyEnchantment getDailyEnchantment(){
        return enchantmentType;
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
    public Integer getItemPrice(Material material){
        if (crops.containsKey(material)) return crops.get(material);
        if (mining.containsKey(material)) return mining.get(material);
        if (drops.containsKey(material)) return drops.get(material);
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
        if(allowed) boughtEnchant.remove(p.getUniqueId());
        else boughtEnchant.add(p.getUniqueId());
    }

    @Override
    public boolean enchantAvailable(){
        return enchantAvailable;
    }

    @Override
    public void setItemPrice(Material material, Integer price){
        if(mining.containsKey(material)) mining.put(material, price);
        if(drops.containsKey(material)) drops.put(material, price);
        if(crops.containsKey(material)) crops.put(material, price);
        throw new IllegalArgumentException(material.toString() + " is not a valid material");
    }

    @Override
    public Integer getEnchantBasePrice(Enchantment enchantment){
        return enchants.get(enchantment);
    }
    @Override
    public void setEnchantBasePrice(Enchantment enchantment, Integer price){
        enchants.put(enchantment, price);
    }

    @Override
    public Inventory getDropInventory(CrownUser user){
        ItemStack header = CrownUtils.makeItem(Material.ROTTEN_FLESH, 1, true, "&bDrops");
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
        ItemStack header = CrownUtils.makeItem(Material.IRON_PICKAXE, 1, true, "&bMining");
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
        ItemStack header = CrownUtils.makeItem(Material.OAK_SAPLING, 1, true, "&bCrops");
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
        ItemStack rod = CrownUtils.makeItem(Material.END_ROD, 1, true, "&7-");
        rod.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        ItemStack purpleGlass = CrownUtils.makeItem(Material.PURPLE_STAINED_GLASS_PANE, 1 ,true, "&7-");

        Inventory inv = getBaseInventory("Black Market: Enchants", rod);

        final ItemStack border = CrownUtils.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "&7-");
        inv.setItem(10, border);
        inv.setItem(16, border);

        inv.setItem(12, rod);
        inv.setItem(14, rod);
        inv.setItem(22, rod);

        inv.setItem(0, purpleGlass);
        inv.setItem(8, purpleGlass);
        inv.setItem(18, purpleGlass);
        inv.setItem(26, purpleGlass);

        ItemStack acceptingOrDenying = getAcceptEnchantButton();
        if(!accepting) acceptingOrDenying = getDenyEnchantButton();
        inv.setItem(13, acceptingOrDenying);

        if(userItem != null) inv.setItem(11, userItem);

        inv.setItem(15, getCoolEnchant());

        return inv;
    }

    @Override
    public ItemStack getAcceptEnchantButton(){
        return CrownUtils.makeItem(Material.LIME_STAINED_GLASS_PANE, 1, true, ChatColor.GREEN + "[Accept and Pay]",
                "&7Enchant the item for " + CrownUtils.decimalizeNumber(getDailyEnchantment().getPrice()) + " Rhines");
    }

    @Override
    public ItemStack getDenyEnchantButton(){
        return CrownUtils.makeItem(Material.RED_STAINED_GLASS_PANE, 1, true, ChatColor.RED + "[Cannot accept]",
                "&7Cannot accept enchantment!");
    }

    @Override
    public Inventory getParrotInventory(){
        Inventory invToOpen = new CustomInventoryHolder("Parrot Shop", 27).getInventory();
        ItemStack pane = CrownUtils.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, ChatColor.GRAY + " ");

        for (int i = 0; i < 10; i++) {
            invToOpen.setItem(i, pane);
        }
        for (int i = 17; i < 27; i++) {
            invToOpen.setItem(i, pane);
        }

        invToOpen.setItem(11, CrownUtils.makeItem(Material.GRAY_WOOL, 1 , true, ChatColor.GRAY + "Gray Parrot", ChatColor.YELLOW + "Value: 50,000 Rhines.", ChatColor.DARK_GRAY + "Do /parrot gray to summon it"));
        invToOpen.setItem(12, CrownUtils.makeItem(Material.GREEN_WOOL, 1, true, ChatColor.GREEN + "Green Parrot", ChatColor.YELLOW + "Value: 50,000 Rhines.", ChatColor.DARK_GRAY + "Do /parrot green to summon it"));
        invToOpen.setItem(13, CrownUtils.makeItem(Material.BLUE_WOOL, 1, true, ChatColor.BLUE + "Blue Parrot", ChatColor.YELLOW + "Value: 100,000 Rhines.", ChatColor.DARK_GRAY + "Do /parrot blue to summon it"));
        invToOpen.setItem(14, CrownUtils.makeItem(Material.RED_WOOL, 1, true, ChatColor.RED + "Red Parrot", ChatColor.YELLOW + "Value: Available only for Captains.", ChatColor.DARK_GRAY + "Do /parrot red to summon it"));
        invToOpen.setItem(15, CrownUtils.makeItem(Material.LIGHT_BLUE_WOOL, 1, true, ChatColor.AQUA + "Aqua Parrot", ChatColor.YELLOW + "Value: Available only for Admirals.", ChatColor.DARK_GRAY + "Do /parrot aqua to summon it"));

        return invToOpen;
    }

    private Inventory getBaseInventory(String name, ItemStack header){
        CustomInventoryHolder holder = new CustomInventoryHolder(name, InventoryType.CHEST);
        Inventory base = holder.getInventory();
        final ItemStack borderItem = CrownUtils.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "&7-");

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
        ItemStack item = CrownUtils.makeItem(material, 1, true, s,
                "&eValue: " + getItemPrice(material) + " Rhines per item,",
                "&6" + getItemPrice(material) * 64 + " Rhines for a stack.",
                "&7Amount of items you will sell: " + user.getSellAmount().toString().toLowerCase().replaceAll("_", " "));

        if(isSoldOut(material)){
            item = CrownUtils.makeItem(material, 1, true, ChatColor.GRAY + s,
                    "&7No longer available!");
        }

        return item;
    }

    private ItemStack getCoolEnchant(){
        ItemStack item = CrownUtils.makeItem(Material.ENCHANTED_BOOK, 1, false, null, "&6Value: &e" + CrownUtils.decimalizeNumber(getDailyEnchantment().getPrice()) + " Rhines&6.");
        item.addUnsafeEnchantment(getDailyEnchantment().getEnchantment(), getDailyEnchantment().getLevel());

        return item;
    }
}
