package net.forthecrown.core.files;

import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.ShopInventory;
import net.forthecrown.core.api.SignShop;
import net.forthecrown.core.enums.ShopType;
import net.forthecrown.core.inventories.CrownShopInventory;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CrownSignShop extends FtcFileManager implements SignShop {

    private final Location location;
    private final Block block;
    private final ShopInventory inventory;

    private UUID owner;
    private Integer price;
    private ShopType type;
    private boolean outOfStock;

    //used by getSignShop
    public CrownSignShop(Location signBlock) throws NullPointerException {
        super(signBlock.getWorld().getName() + "_" + signBlock.getBlockX() + "_" + signBlock.getBlockY() + "_" + signBlock.getBlockZ(), "shopdata");

        //file doesn't exist nor does the legacy file, there for go fuck yourself
        if (fileDoesntExist && !legacyFileExists()) {
            delete();
            throw new NullPointerException("Could not load shop file! Named, " + fileName);
        }

        this.location = signBlock;
        this.block = signBlock.getBlock();

        inventory = new CrownShopInventory(this);
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

        inventory = new CrownShopInventory(this);
        FtcCore.loadedShops.add(this);
    }

    @Override
    public void save() {
        if(wasDeleted()) return;

        getFile().set("Owner", getOwner().toString());
        getFile().set("Location", getLocation());
        getFile().set("Type", getType().toString());
        getFile().set("Price", getPrice());
        getFile().set("ExampleItem", getInventory().getExampleItem());
        getFile().set("OutOfStock", isOutOfStock());
        getFile().set("ItemList", getInventory().getShopContents());

        super.save();
    }

    @Override
    public void reload() {
        super.reload();

        setOwner(UUID.fromString(getFile().getString("Owner")));
        setType(ShopType.valueOf(getFile().getString("Type")));
        setPrice(getFile().getInt("Price"));
        if(getFile().get("GetOutOfStock") != null) setOutOfStock(getFile().getBoolean("OutOfStock"));

        try{
            inventory.setShopContents((List<ItemStack>) getFile().getList("ItemList"));
            getInventory().setExampleItem(getFile().getItemStack("ExampleItem"));

            if(inventory.getShopContents().size() > 0) setOutOfStock(false);
        } catch (Exception e){
            inventory.setShopContents(new ArrayList<>());
            setOutOfStock(true);
        }

        updateSign();
    }

    @Override
    public void destroyShop() {
        if(inventory.getShopContents().size() > 0) {
            for (ItemStack stack : inventory.getShopContents()){ location.getWorld().dropItemNaturally(location, stack); }
            location.getWorld().spawnParticle(Particle.CLOUD, location.add(0.5, 0.5, 0.5), 5, 0.1D, 0.1D, 0.1D, 0.05D);
        }

        delete();
        FtcCore.loadedShops.remove(this);
    }

    @Override
    public Inventory getExampleInventory(){
        Inventory inv = Bukkit.createInventory(getInventory().getHolder(), InventoryType.HOPPER, "Specify what and how much");
        inv.setItem(0, CrownUtils.makeItem(Material.BARRIER, 1, true, ""));
        inv.setItem(1, CrownUtils.makeItem(Material.BARRIER, 1, true, ""));
        inv.setItem(3, CrownUtils.makeItem(Material.BARRIER, 1, true, ""));
        inv.setItem(4, CrownUtils.makeItem(Material.BARRIER, 1, true, ""));

        return inv;
    }

    @Override
    public String getName(){
        return fileName;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public void setOwner(UUID shopOwner) {
        this.owner = shopOwner;
    }

    @Override
    public ShopType getType() {
        return type;
    }

    @Override
    public void setType(ShopType shopType) {
        this.type = shopType;
    }

    @Override
    public Integer getPrice() {
        return price;
    }

    @Override
    public void setPrice(Integer price) {
        setPrice(price, false);
    }
    @Override
    public void setPrice(Integer price, boolean updateSign) {
        this.price = price;

        if(updateSign) updateSign();
    }

    @Override
    public boolean isOutOfStock() {
        return outOfStock;
    }

    @Override
    public void setOutOfStock(boolean outOfStock) {
        if(getType().equals(ShopType.ADMIN_SELL_SHOP)) return;

        this.outOfStock = outOfStock;
        updateSign();
    }

    @Override
    public boolean wasDeleted(){
        return deleted;
    }

    @Override
    public ShopInventory getInventory() {
        return inventory;
    }

    @Override
    public Sign getSign(){
        return (Sign) getBlock().getState();
    }

    @Override
    public void updateSign(){
        Sign s = getSign();

        String ln1 = getType().getInStockLabel();
        if(isOutOfStock()) ln1 = getType().getOutOfStockLabel();

        String ln3 = ChatColor.DARK_GRAY + "Price: " + ChatColor.RESET + "$" + getPrice();

        s.setLine(0, ln1);
        s.setLine(3, ln3);

        Bukkit.getScheduler().runTask(FtcCore.getInstance(), () -> s.update(true));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignShop that = (SignShop) o;
        return getLocation().equals(that.getLocation()) &&
                getOwner().equals(that.getOwner()) &&
                getPrice().equals(that.getPrice()) &&
                getType() == that.getType() &&
                getInventory().equals(that.getInventory());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLocation(), getOwner(), getPrice(), getType());
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

        for(ItemStack stack : (List<ItemStack>) oldConfig.getList("Inventory.content")){
            if(stack == null) continue;
            inventory.addItem(stack);
        }

        inventory.setExampleItem((ItemStack) oldConfig.getList("Inventory.shop").get(0));
        if(inventory.getExampleItem() == null){
            if(inventory.getShopContents().size() > 0) getInventory().setExampleItem(inventory.getShopContents().get(0));
        }

        oldFile.delete();

        save();
        reload();
    }
}
