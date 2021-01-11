package net.forthecrown.core.economy.files;

import net.forthecrown.core.enums.ShopType;
import net.forthecrown.core.files.FtcFileManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class SignShop extends FtcFileManager {

    private final Location location;
    private final Block block;
    private final String fileName;

    private UUID owner;
    private Integer price;
    private ShopType type;
    private boolean outOfStock;
    private ItemStack exampleItem;
    private Inventory shopInv;
    private List<ItemStack> contents = new ArrayList<>();

    public static final Set<SignShop> loadedShops = new HashSet<>();

    private boolean wasDeleted = false;

    //used by getSignShop
    public SignShop(Location signBlock) throws NullPointerException {
        super(signBlock.getWorld().getName() + "_" + signBlock.getBlockX() + "_" + signBlock.getBlockY() + "_" + signBlock.getBlockZ(), "shopdata", true);
        this.fileName = signBlock.getWorld().getName() + "_" + signBlock.getBlockX() + "_" + signBlock.getBlockY() + "_" + signBlock.getBlockZ();

        //file doesn't exist nor does the legacy file, there for go fuck yourself
        if (fileDoesntExist && !legacyFileExists()) throw new NullPointerException();

        this.location = signBlock;
        this.block = signBlock.getBlock();

        loadedShops.add(this);
        //if (legacyFileExists()) convertLegacy();
        //else
        reload();
    }

    //used by createSignShop
    public SignShop(Location location, ShopType shopType, Integer price, UUID shopOwner) {
        super(location.getWorld().getName() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ(), "shopdata");
        fileName = location.getWorld().getName() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
        this.location = location;
        this.block = location.getBlock();
        this.type = shopType;
        this.price = price;
        this.owner = shopOwner;

        if(type != ShopType.ADMIN_BUY_SHOP && type != ShopType.ADMIN_SELL_SHOP) this.outOfStock = true;

        getFile().addDefault("Owner", owner.toString());
        getFile().addDefault("Location", location);
        getFile().addDefault("Type", type.toString());
        getFile().addDefault("ItemList", new ArrayList<>());
        getFile().options().copyDefaults(true);
        super.save();

        loadedShops.add(this);
        initInv();
    }

    public void save() {
        if(wasDeleted) return;
        getFile().set("Owner", getOwner().toString());
        getFile().set("Location", getLocation());
        getFile().set("Type", getType().toString());
        getFile().set("Price", getPrice());
        getFile().set("ExampleItem", getExampleItem());

        List<ItemStack> tempList = new ArrayList<>();
        for(ItemStack stack : getShopInventory().getContents()){
            if(stack == null) continue;
            tempList.add(stack);
        }

        getFile().set("ItemList", tempList);

        super.save();
    }

    public void reload() {
        super.reload();

        setOwner(UUID.fromString(getFile().getString("Owner")));
        setType(ShopType.valueOf(getFile().getString("Type")));
        setPrice(getFile().getInt("Price"));

        ItemStack exItem;
        try{
            contents = (List<ItemStack>) getFile().getList("ItemList");
            exItem = getFile().getItemStack("ExampleItem");
        } catch (IndexOutOfBoundsException e){
            contents = new ArrayList<>();
            exItem = null;
        }
        setExampleItem(exItem);

        Sign sign = (Sign) getBlock().getState();
        sign.setLine(3, org.bukkit.ChatColor.DARK_GRAY + "Price: " + org.bukkit.ChatColor.WHITE + "$" + getPrice());
        sign.update();

        initInv();
    }

    public Inventory getShopInventory(){
        return shopInv;
    }

    public Inventory getExampleInventory(){
        return Bukkit.createInventory(null, InventoryType.HOPPER, "Specify what to sell and how much:");
    }

    public void setExampleItems(ItemStack[] exampleItem){
        ItemStack item = null;
        for(ItemStack stack : exampleItem){
            if(stack != null) {
                getShopInventory().setItem(getShopInventory().firstEmpty(), stack);
                if(item == null) item = stack;
            }
        }
        if(item == null) throw new NullPointerException();
        setExampleItem(item);

        if(type != ShopType.ADMIN_BUY_SHOP && type != ShopType.ADMIN_SELL_SHOP) setOutOfStock(false);
        save();
    }

    public ItemStack[] setItems(ItemStack[] toSet){
        List<ItemStack> toReturn = new ArrayList<>();
        if(toSet.length > 27) return null;
        if(isOutOfStock()) setExampleItem(toSet[0]);

        for(ItemStack stack : toSet){
            if(stack == null) continue;
            if(stack.getType() != getExampleItem().getType()) {
                toReturn.add(stack);
                continue;
            }
            getShopInventory().setItem(getShopInventory().firstEmpty(), stack);
            setOutOfStock(false);
        }

        save();

        ItemStack[] asd = new ItemStack[toReturn.size()];
        toReturn.toArray(asd);
        return asd;
    }

    public Location getLocation() {
        return location;
    }

    public Block getBlock() {
        return block;
    }

    public void destroyShop() {
        if(contents.size() > 0) {
            for (ItemStack stack : getShopInventory()) location.getWorld().dropItemNaturally(location, stack);
            location.getWorld().spawnParticle(Particle.CLOUD, location.add(0.5, 0.5, 0.5), 5, 0.1D, 0.1D, 0.1D, 0.05D);
        }

        delete();
        wasDeleted = true;
        loadedShops.remove(this);
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID shopOwner) {
        this.owner = shopOwner;
    }

    public ShopType getType() {
        return type;
    }

    public void setType(ShopType shopType) {
        this.type = shopType;
        initInv();
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public boolean isOutOfStock() {
        return outOfStock;
    }

    public void setOutOfStock(boolean outOfStock) {
        if(getType() == ShopType.ADMIN_BUY_SHOP || getType() == ShopType.ADMIN_SELL_SHOP) return;
        this.outOfStock = outOfStock;

        Sign sign = (Sign) block.getState();
        if(outOfStock) sign.setLine(0, ChatColor.translateAlternateColorCodes('&', "&4" + ChatColor.stripColor(sign.getLine(0))));
        else sign.setLine(0, ChatColor.translateAlternateColorCodes('&', "&a" + ChatColor.stripColor(sign.getLine(0))));
        sign.update();
    }

    public ItemStack getExampleItem() {
        return exampleItem;
    }

    public void setExampleItem(ItemStack exampleItem) {
        this.exampleItem = exampleItem;
    }






    private void initInv(){
        shopInv = Bukkit.createInventory(null, 27, "Shop content:");
        for(ItemStack stack : contents){
            if(shopInv.firstEmpty() == -1) break;
            shopInv.setItem(shopInv.firstEmpty(), stack);
        }
    }

    private boolean legacyFileExists() {
        File oldFile = new File("plugin/ShopsReworked/ShopData/" + fileName + ".yml");
        return oldFile.exists();
    }

    private void convertLegacy() { //I have no idea
        File oldFile = new File("plugin/ShopsReworked/ShopData/" + fileName + ".yml");
        FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);

        fileConfig.set("Owner", UUID.fromString(oldConfig.getString("Player")));

        ConfigurationSection locationSec = oldConfig.getConfigurationSection("Location");
        Location shopLoc = new Location(Bukkit.getWorld(locationSec.getString("world")), locationSec.getInt("x"), locationSec.getInt("y"), locationSec.getInt("z"));
        fileConfig.set("Location", shopLoc);

        List<ItemStack> itemsList = new ArrayList<>();
        for(ItemStack stack : (List<ItemStack>) oldConfig.getList("Inventory")){
            if(stack != null) itemsList.add(stack);
        }
        fileConfig.set("ItemList", itemsList);

        super.save();
        reload();
    }
}
