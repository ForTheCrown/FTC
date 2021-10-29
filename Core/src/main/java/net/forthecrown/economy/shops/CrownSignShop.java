package net.forthecrown.economy.shops;

import net.forthecrown.core.Crown;
import net.forthecrown.inventory.FtcItems;
import net.forthecrown.serializer.AbstractYamlSerializer;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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

public class CrownSignShop extends AbstractYamlSerializer implements SignShop {

    private final WorldVec3i loc;
    private final Block block;
    private final CrownShopInventory inventory;

    private UUID owner;
    private Integer price;
    private ShopType type;
    private boolean outOfStock;

    //used by getSignShop
    public CrownSignShop(WorldVec3i loc) throws NullPointerException {
        super(FtcUtils.locationToFilename(loc.toLocation()), "shopdata", true);

        //file doesn't exist there for go fuck yourself
        if (fileDoesntExist) throw new NullPointerException("Could not load shop file! Named, " + fileName);

        this.loc = loc;
        block = loc.getBlock();

        inventory = new CrownShopInventory(this);
        Crown.getShopManager().addShop(this);

        reload();
    }

    //used by createSignShop
    public CrownSignShop(WorldVec3i loc, ShopType shopType, Integer price, UUID shopOwner) {
        super(FtcUtils.locationToFilename(loc.toLocation()), "shopdata", false);

        this.loc = loc;
        this.price = price;
        block = loc.getBlock();
        type = shopType;
        owner = shopOwner;

        if(type != ShopType.ADMIN_BUY && type != ShopType.ADMIN_SELL) this.outOfStock = true;

        getFile().addDefault("Owner", owner.toString());
        getFile().addDefault("Location", loc);
        getFile().addDefault("Type", type.toString());
        getFile().addDefault("ItemList", new ArrayList<>());
        getFile().options().copyDefaults(true);
        save(false);

        inventory = new CrownShopInventory(this);
        Crown.getShopManager().addShop(this);
    }

    @Override
    public void saveFile() {
        getFile().set("Owner", getOwner().toString());
        getFile().set("Location", getLocation());
        getFile().set("Type", getType().toString());
        getFile().set("Price", getPrice());
        getFile().set("ExampleItem", getInventory().getExampleItem());
        getFile().set("OutOfStock", isOutOfStock());
        getFile().set("ItemList", getInventory().getShopContents());
    }

    @Override
    public void reloadFile() {
        setOwner(UUID.fromString(getFile().getString("Owner")));
        setType(ShopType.valueOf(getFile().getString("Type").replaceAll("_SHOP", "")));
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

        inventory.checkStock();
        update();
    }

    @Override
    public void destroy(boolean removeBlock) {
        Crown.getShopManager().removeShop(this);
        if(inventory != null && inventory.getShopContents().size() > 0) {
            for (ItemStack stack : inventory.getShopContents()){ loc.getWorld().dropItemNaturally(loc.toLocation(), stack); }
            loc.getWorld().spawnParticle(Particle.CLOUD, loc.toLocation().add(0.5, 0.5, 0.5), 5, 0.1D, 0.1D, 0.1D, 0.05D);
        }

        if(removeBlock)getBlock().breakNaturally();
        delete();
    }

    @Override
    public void unload(){
        Crown.getShopManager().removeShop(this);
        save();
    }

    @Override
    public Inventory getExampleInventory(){
        Inventory inv = Bukkit.createInventory(this, InventoryType.HOPPER, Component.text("Specify what and how much"));
        inv.setItem(0, FtcItems.makeItem(Material.BARRIER, 1, true, ""));
        inv.setItem(1, FtcItems.makeItem(Material.BARRIER, 1, true, ""));
        inv.setItem(3, FtcItems.makeItem(Material.BARRIER, 1, true, ""));
        inv.setItem(4, FtcItems.makeItem(Material.BARRIER, 1, true, ""));

        return inv;
    }

    @Override
    public WorldVec3i getShopLocation() {
        return loc;
    }

    @Override
    public String getName(){
        return fileName;
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
        if(getType().equals(ShopType.ADMIN_SELL)) return;

        this.outOfStock = outOfStock;
        update();
    }

    @Override
    public boolean wasDeleted(){
        return deleted;
    }

    @Nonnull
    @Override
    public CrownShopInventory getInventory() {
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

        s.line(0, ln1);
        s.line(3, Crown.getShopManager().getPriceLine(price));

        Bukkit.getScheduler().runTask(Crown.inst(), () -> s.update(true));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignShop that = (SignShop) o;
        return getShopLocation().equals(that.getShopLocation()) &&
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
