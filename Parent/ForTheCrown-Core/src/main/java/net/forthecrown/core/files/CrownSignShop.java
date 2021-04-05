package net.forthecrown.core.files;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.ShopManager;
import net.forthecrown.core.api.ShopInventory;
import net.forthecrown.core.api.SignShop;
import net.forthecrown.core.enums.ShopType;
import net.forthecrown.core.inventories.CrownShopInventory;
import net.forthecrown.core.utils.CrownItems;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CrownSignShop extends AbstractSerializer<FtcCore> implements SignShop {

    private final Location location;
    private final Block block;
    private final CrownShopInventory inventory;

    private UUID owner;
    private Integer price;
    private ShopType type;
    private boolean outOfStock;

    //used by getSignShop
    public CrownSignShop(Location location) throws NullPointerException {
        super(location.getWorld().getName() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ(), "shopdata", true, FtcCore.getInstance());

        //file doesn't exist there for go fuck yourself
        if (fileDoesntExist) throw new NullPointerException("Could not load shop file! Named, " + fileName);

        this.location = location;
        this.block = location.getBlock();

        inventory = new CrownShopInventory(this);
        ShopManager.LOADED_SHOPS.put(location, this);

        reload();
    }

    //used by createSignShop
    public CrownSignShop(Location location, ShopType shopType, Integer price, UUID shopOwner) {
        super(location.getWorld().getName() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ(), "shopdata", false, FtcCore.getInstance());
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
        ShopManager.LOADED_SHOPS.put(location, this);
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

        try {
            super.save();
        } catch (NullPointerException e){
            ShopManager.LOADED_SHOPS.remove(this.getLocation());
        }
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

        update();
    }

    @Override
    public void destroy(boolean removeBlock) {
        ShopManager.LOADED_SHOPS.remove(this.getLocation());
        if(inventory != null && inventory.getShopContents().size() > 0) {
            for (ItemStack stack : inventory.getShopContents()){ location.getWorld().dropItemNaturally(location, stack); }
            location.getWorld().spawnParticle(Particle.CLOUD, location.add(0.5, 0.5, 0.5), 5, 0.1D, 0.1D, 0.1D, 0.05D);
        }

        if(removeBlock)getBlock().breakNaturally();
        delete();
    }

    @Override
    public Inventory getExampleInventory(){
        Inventory inv = Bukkit.createInventory(this, InventoryType.HOPPER, Component.text("Specify what and how much"));
        inv.setItem(0, CrownItems.makeItem(Material.BARRIER, 1, true, ""));
        inv.setItem(1, CrownItems.makeItem(Material.BARRIER, 1, true, ""));
        inv.setItem(3, CrownItems.makeItem(Material.BARRIER, 1, true, ""));
        inv.setItem(4, CrownItems.makeItem(Material.BARRIER, 1, true, ""));

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

        if(updateSign) update();
    }

    @Override
    public boolean isOutOfStock() {
        return outOfStock;
    }

    @Override
    public void setOutOfStock(boolean outOfStock) {
        if(getType().equals(ShopType.ADMIN_SELL_SHOP)) return;

        this.outOfStock = outOfStock;
        update();
    }

    @Override
    public boolean wasDeleted(){
        return deleted;
    }

    @Nonnull
    @Override
    public ShopInventory getInventory() {
        return inventory;
    }

    @Override
    public Sign getSign(){
        return (Sign) getBlock().getState();
    }

    @Override
    public void update(){
        Sign s = getSign();
        Component ln1 = getType().inStockLabel();
        if(isOutOfStock()) ln1 = getType().outOfStockLabel();
        String ln3 = ChatColor.DARK_GRAY + "Price: " + ChatColor.RESET + "$" + getPrice();

        s.line(0, ln1);
        s.line(3, Component.text(ln3));

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
}
