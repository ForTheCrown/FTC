package net.forthecrown.core.files;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.SignShop;
import net.forthecrown.core.enums.ShopType;
import net.forthecrown.core.inventories.CrownShopStock;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class CrownSignShop extends FtcFileManager implements SignShop {

    private final Location location;
    private final Block block;

    private UUID owner;
    private Integer price;
    private ShopType type;
    private boolean outOfStock;
    private final CrownShopStock stock;

    //used by getSignShop
    public CrownSignShop(Location signBlock) throws NullPointerException {
        super(signBlock.getWorld().getName() + "_" + signBlock.getBlockX() + "_" + signBlock.getBlockY() + "_" + signBlock.getBlockZ(), "shopdata");

        //file doesn't exist nor does the legacy file, there for go fuck yourself
        if (fileDoesntExist && !legacyFileExists()) {
            super.delete();
            throw new NullPointerException("Could not load shop file! Named, " + fileName);
        }

        this.location = signBlock;
        this.block = signBlock.getBlock();

        stock = new CrownShopStock(this);

        FtcCore.loadedShops.add(this);
        if (legacyFileExists()) convertLegacy();
        else reload();
    }

    //used by createSignShop
    public CrownSignShop(Location location, ShopType shopType, Integer price, UUID shopOwner) {
        super(location.getWorld().getName() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ(), "shopdata");
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

        stock = new CrownShopStock(this);
        FtcCore.loadedShops.add(this);
    }

    public void save() {
        if(deleted) return;
        getFile().set("Owner", getOwner().toString());
        getFile().set("Location", getLocation());
        getFile().set("Type", getType().toString());
        getFile().set("Price", getPrice());
        getFile().set("ExampleItem", getStock().getExampleItem());
        getFile().set("OutOfStock", isOutOfStock());
        getFile().set("ItemList", getStock().getContents());

        super.save();
    }

    public void reload() {
        super.reload();

        setOwner(UUID.fromString(getFile().getString("Owner")));
        setType(ShopType.valueOf(getFile().getString("Type")));
        setPrice(getFile().getInt("Price"));
        if(getFile().get("GetOutOfStock") != null) setOutOfStock(getFile().getBoolean("OutOfStock"));

        try{
            stock.setContents((List<ItemStack>) getFile().getList("ItemList"));
            getStock().setExampleItem(getFile().getItemStack("ExampleItem"));

            if(stock.getContents().size() > 0) setOutOfStock(false);
        } catch (IndexOutOfBoundsException e){
            stock.setContents(new ArrayList<>());
            setOutOfStock(true);
        }

        Sign sign = (Sign) getBlock().getState();
        sign.setLine(3, ChatColor.DARK_GRAY + "Price: " + ChatColor.RESET + "$" + getPrice());
        sign.update();
    }

    public void destroyShop() {
        if(stock.getContents().size() > 0) {
            for (ItemStack stack : stock.getContents()){ location.getWorld().dropItemNaturally(location, stack); }
            location.getWorld().spawnParticle(Particle.CLOUD, location.add(0.5, 0.5, 0.5), 5, 0.1D, 0.1D, 0.1D, 0.05D);
        }

        super.delete();
        FtcCore.loadedShops.remove(this);
    }

    public Inventory getShopInventory(){
        Bukkit.broadcastMessage(stock.getContents().toString());
        Inventory shopInv = Bukkit.createInventory(null, 27, "Shop Contents");
        int i = 0;
        for (ItemStack item : stock.getContents()){
            shopInv.setItem(i, item);
            i++;
        }

        return shopInv;
    }

    public Inventory getExampleInventory(){
        Inventory inv = Bukkit.createInventory(null, InventoryType.HOPPER, "Specify what and how much");
        inv.setItem(0, FtcCore.makeItem(Material.BARRIER, 1, true, ""));
        inv.setItem(1, FtcCore.makeItem(Material.BARRIER, 1, true, ""));
        inv.setItem(3, FtcCore.makeItem(Material.BARRIER, 1, true, ""));
        inv.setItem(4, FtcCore.makeItem(Material.BARRIER, 1, true, ""));

        return inv;
    }

/*
    public boolean setExampleItems(ItemStack[] exampleItem){
        ItemStack item = null;
        for(ItemStack stack : exampleItem){
            if(stack == null) continue;
            if(item == null) item = stack;
            else return false;
        }
        if(item == null) throw new NullPointerException();
        setExampleItem(item);

        stock.add(item);

        setOutOfStock(false);
        save();
        return true;
    }

    public ItemStack[] setItems(ItemStack[] toSet){
        List<ItemStack> toReturn = new ArrayList<>();
        if(toSet.length > 27) return null;
        if(getExampleItem() == null) setExampleItem(toSet[0]);

        for(ItemStack stack : toSet){
            if(stack == null) continue;
            if(stack.getType() != getExampleItem().getType()) {
                toReturn.add(stack);
                continue;
            }
            stock.add(stack);
            setOutOfStock(false);
        }

        ItemStack[] asd = new ItemStack[toReturn.size()];
        toReturn.toArray(asd);
        return asd;
    }
 */

    public Location getLocation() {
        return location;
    }

    public Block getBlock() {
        return block;
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
        if(getType().equals(ShopType.ADMIN_SELL_SHOP)) return;

        this.outOfStock = outOfStock;

        Sign sign = (Sign) getBlock().getState();
        if(outOfStock)sign.setLine(0, getType().getOutOfStockLabel());
        else sign.setLine(0, getType().getInStockLabel());

        sign.update(true);
    }

    public boolean wasDeleted(){
        return deleted;
    }

    public CrownShopStock getStock() {
        return stock;
    }

    public Sign getSign(){
        return (Sign) getBlock().getState();
    }


    private boolean legacyFileExists() {
        File oldFile = new File("plugins/ShopsReworked/ShopData/" + fileName + ".yml");
        return oldFile.exists();
    }

    private void convertLegacy() { //I have no idea
        File oldFile = new File("plugins/ShopsReworked/ShopData/" + fileName + ".yml");
        FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);

        Sign sign = (Sign) getBlock().getState();
        String line1 = sign.getLine(0).toLowerCase();

        setOwner(UUID.fromString(oldConfig.getString("Player")));

        if(line1.contains("=[buy]=")) setType(ShopType.ADMIN_BUY_SHOP);
        else if(line1.contains("=[sell]=")) setType(ShopType.ADMIN_SELL_SHOP);
        else if(line1.contains("-[sell]-")) setType(ShopType.SELL_SHOP);
        else setType(ShopType.BUY_SHOP);

        if(line1.contains(ChatColor.DARK_RED + "buy") || line1.contains(ChatColor.RED + "buy")) setOutOfStock(true);

        try {
            setPrice(Integer.parseInt(ChatColor.stripColor(sign.getLine(3)).replaceAll("[\\D]", "").replaceAll("\\$", "")));
        } catch (NullPointerException e){ setPrice(500); }

        List<ItemStack> tempList = new ArrayList<>();
        if(oldConfig.getList("Inventory.content").size() != 0){
            for(ItemStack stack : (List<ItemStack>) oldConfig.getList("Inventory.content")){
                if(stack == null) continue;
                tempList.add(stack);
            }
        } else setOutOfStock(true);

        stock.setContents(tempList);

        if(oldConfig.getItemStack("Inventory.shop") != null) getStock().setExampleItem(oldConfig.getItemStack("Inventory.shop"));

        if(oldConfig.getItemStack("Inventory.shop") == null){
            try {
                ItemStack stack = getStock().getExampleItem();
                stack.setAmount(Integer.parseInt(ChatColor.stripColor(sign.getLine(1)).replaceAll("[\\D]", "")));
                stock.setExampleItem(stack);
            } catch (Exception e){
                try {
                    ItemStack stack = getStock().getExampleItem();
                    stack.setAmount(Integer.parseInt(ChatColor.stripColor(sign.getLine(1)).replaceAll("[\\D]", "")));
                    getStock().setExampleItem(stack);
                } catch (Exception ignored) {}
            }
        }

        sign.setLine(0, getType().getInStockLabel());
        sign.update();

        oldFile.delete();

        save();
        reload();
    }
}
