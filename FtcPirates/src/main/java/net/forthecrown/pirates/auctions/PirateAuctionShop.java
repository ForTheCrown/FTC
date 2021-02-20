package net.forthecrown.pirates.auctions;

import net.forthecrown.core.files.FtcFileManager;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PirateAuctionShop extends FtcFileManager {

    private AuctionType type;
    private ItemStack sellItem;
    private UUID owner;
    private final Location location;

    private Integer startingBid;
    private Map<UUID, Integer> bids = new HashMap<>();
    private long expiresIn;

    //used for getting
    public PirateAuctionShop(Location sign) {
        super(
                "auction_" + sign.getWorld().getName() + "_" + sign.getBlockX() + "_" + sign.getBlockY() + "_" + sign.getBlockZ()
                , "Auctions"
        );
        this.location = sign;

        if(!fileDoesntExist){
            delete();
            throw new NullPointerException(getFile().getName() + " doesn't exist");
        }
        reload();
    }

    //used for creating
    public PirateAuctionShop(Location sign, AuctionType type, Integer startingBid){
        super(
                "auction_" + sign.getWorld().getName() + "_" + sign.getBlockX() + "_" + sign.getBlockY() + "_" + sign.getBlockZ()
                , "Auctions"
        );

        this.location = sign;
        this.type = type;
        this.startingBid = startingBid;

        save();
    }

    @Override
    public void save() {
        getFile().set("Type", getType().toString());

        if(getOwner() != null) getFile().set("Owner", getOwner().toString());
        else getFile().set("Owner", "UNCLAIMED");

        if(getSellItem() != null) getFile().set("Item", getSellItem());
        else getFile().set("Item", "none");

        if(getBids().size() > 0){
            Map<String, Integer> tempMap = new HashMap<>();
            for (UUID id: getBids().keySet()){
                tempMap.put(id.toString(), getBids().get(id));
            }
            getFile().createSection("Bidders", tempMap);
        }

        if(startingBid != null) getFile().set("StartingBid", getStartingBid());
        else getFile().set("StartingBid", "none");

        super.save();
    }

    @Override
    public void reload() {
        super.reload();

        setType(AuctionType.valueOf(getFile().getString("Type")));

        String fileOwner = getFile().getString("Owner");
        if(fileOwner.equalsIgnoreCase("UNCLAIMED")) setOwner(null);
        else setOwner(UUID.fromString(fileOwner));

        try {
            ItemStack fileItem = getFile().getItemStack("Item");
            setSellItem(fileItem);
        } catch (Exception e){
            setSellItem(null);
        }
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public boolean isUnclaimed(){
        return getOwner() == null;
    }

    public Sign getSign(){
        return (Sign) getLocation().getBlock().getState();
    }

    public void updateSign(){
        String ln1 = getType().getLabel();
        if(isUnclaimed()) ln1 = getType().getUnclaimedLabel();
    }

    public Location getLocation() {
        return location;
    }

    public AuctionType getType() {
        return type;
    }

    public void setType(AuctionType type) {
        this.type = type;
    }

    public ItemStack getSellItem() {
        return sellItem;
    }

    public void setSellItem(ItemStack sellItem) {
        this.sellItem = sellItem;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public Integer getStartingBid() {
        return startingBid;
    }

    public void setStartingBid(Integer startingBid) {
        this.startingBid = startingBid;
    }

    public Map<UUID, Integer> getBids() {
        return bids;
    }

    public void setBids(Map<UUID, Integer> bids) {
        this.bids = bids;
    }

}
