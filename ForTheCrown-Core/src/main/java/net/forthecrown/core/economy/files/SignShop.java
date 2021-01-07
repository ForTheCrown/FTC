package net.forthecrown.core.economy.files;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.economy.ShopType;
import net.forthecrown.core.files.FtcFileManager;
import net.forthecrown.core.files.FtcUserData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class SignShop extends FtcFileManager {

    private final Location location;
    private final Block block;
    private final String fileName;

    private UUID owner;
    private Integer price;
    private int size;
    private ShopType type;
    private List<ItemStack> contents = new ArrayList<>();
    private Material allowedItem;
    private int itemChangeAmount;

    public static final Set<SignShop> loadedShops = new HashSet<>();

    //used by getSignShop
    public SignShop(Block signBlock) {
        super(signBlock.getWorld().toString() + "_" + signBlock.getLocation().getBlockX() + "_" + signBlock.getLocation().getBlockY() + "_" + signBlock.getLocation().getBlockZ(), "shopdata");
        this.fileName = signBlock.getWorld().toString() + "_" + signBlock.getLocation().getBlockX() + "_" + signBlock.getLocation().getBlockY() + "_" + signBlock.getLocation().getBlockZ();

        if (needsDefaults && !legacyFileExists()) { //file doesn't exist nor does the legacy file, there for go fuck yourself
            file.delete();
            throw new NullPointerException();
        }

        this.location = signBlock.getLocation();
        this.block = signBlock;

        loadedShops.add(this);
        if (legacyFileExists()) convertLegacy();
        else reload();
    }

    //used by createSignShop
    public SignShop(Location location, ShopType shopType, Integer price, UUID shopOwner) {
        super(location.getWorld().toString() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ(), "shopdata");
        fileName = location.getWorld().toString() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
        this.location = location;
        this.block = location.getBlock();
        this.type = shopType;
        this.price = price;
        this.owner = shopOwner;

        fileConfig.addDefault("Owner", getOwner().toString());
        fileConfig.addDefault("Location", getLocation());
        fileConfig.addDefault("Size", 27);
        fileConfig.addDefault("Type", getType());
        fileConfig.addDefault("ItemList", getContents());

        loadedShops.add(this);
        save();
    }

    public void save() {
        fileConfig.set("Owner", getOwner().toString());
        fileConfig.set("Location", getLocation());
        fileConfig.set("Size", getSize());
        fileConfig.set("Type", getType().toString());
        fileConfig.set("ItemList", getContents());

        super.save();
    }

    public void reload() {
        super.reload();

        owner = UUID.fromString(fileConfig.getString("Owner"));
        size = fileConfig.getInt("Size");
        type = ShopType.valueOf(fileConfig.getString("Type"));
        itemChangeAmount = fileConfig.getInt("ItemChangeAmount");

        List<ItemStack> list = (List<ItemStack>) fileConfig.getList("ItemList");
        if (list == null) return;
        contents.addAll(list);

        allowedItem = getContents().get(0).getType();
    }

    public boolean useShop(Player customer) {
        FtcUserData user = FtcCore.getUserData(customer.getUniqueId());
        FtcUserData ownerUser = FtcCore.getUserData(owner);
        String prefix = FtcCore.getPrefix();

        switch (type) {
            case SELL_SHOP:
                break;
            case BUY_SHOP:
                if (user.getBalance() < getPrice()) {
                    customer.sendMessage(prefix + "You poor!");
                    return false;
                }
                if (getContents().size() <= 0) {
                    customer.sendMessage(prefix + "This shop is out of stock");
                    return false;
                }

                user.setBalance(user.getBalance() - getPrice());
                ownerUser.setBalance(ownerUser.getBalance() + getPrice());

                try {
                    customer.getInventory().addItem(getContents().get(getContents().size() - 1));
                } catch (Exception e) {
                    customer.sendMessage(prefix + "Your inventory is full!");
                    return false;
                }

                break;
            case ADMIN_SELL_SHOP:
                break;
            case ADMIN_BUY_SHOP:
                break;
        }
        return true;
    }

    public void setExampleItem(ItemStack exampleItem){
        contents.add(exampleItem);
        allowedItem = exampleItem.getType();
        itemChangeAmount = exampleItem.getAmount();
    }

    public boolean addItems(ItemStack[] toAdd){
        for(ItemStack stack : toAdd){
            if(stack == null) continue;
            if(stack.getType() != allowedItem) return false;
            contents.add(stack);
        }
        return true;
    }

    public Location getLocation() {
        return location;
    }

    public Block getBlock() {
        return block;
    }

    public void destroyShop() {
        for (ItemStack stack : getContents()) location.getWorld().dropItemNaturally(location, stack);
        location.getWorld().spawnParticle(Particle.CLOUD, location.add(0.5, 0.5, 0.5), 5, 0.1D, 0.1D, 0.1D, 0.05D);

        file.delete();
        loadedShops.remove(this);
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID shopOwner) {
        this.owner = shopOwner;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int shopSize) {
        this.size = shopSize;
    }

    public ShopType getType() {
        return type;
    }

    public void setType(ShopType shopType) {
        this.type = shopType;
    }

    public List<ItemStack> getContents() {
        return contents;
    }

    public void setContents(List<ItemStack> shopStorage) {
        this.contents = shopStorage;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Material getAllowedItem() {
        return allowedItem;
    }

    public void setAllowedItem(Material allowedItems) {
        this.allowedItem = allowedItems;
    }


    private boolean legacyFileExists() {
        File oldFile = new File("plugin/ShopsReworked/ShopData/" + fileName + ".yml");
        return oldFile.exists();
    }

    private void convertLegacy() {
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
