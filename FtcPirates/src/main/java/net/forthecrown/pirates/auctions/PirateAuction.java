package net.forthecrown.pirates.auctions;

import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.core.files.AbstractSerializer;
import net.forthecrown.pirates.Pirates;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PirateAuction extends AbstractSerializer<Pirates> implements Auction {

    private String name;
    private final Sign sign;
    private final Location location;

    private CrownUser highestBidder;
    private CrownUser owner;
    private Map<UUID, Integer> bids = new HashMap<>();
    private ItemStack item;
    private Entity displayEntity;

    private int highestBid;
    private int baseBid;
    private boolean waitingForItemClaim;
    private long expiresAt;
    private boolean adminAuction;

    //gets
    public PirateAuction(Location location) {
        super("auction_" + location.getWorld().getName() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ(), "auctions", true, Pirates.plugin);

        if (fileDoesntExist) throw new NullPointerException(getFile().getName() + " doesn't exist, throwing exception");

        this.sign = (Sign) location.getBlock().getState();
        this.location = location;

        reload();
        AuctionManager.AUCTIONS.put(name, this);
    }


    //creates
    public PirateAuction(Location location, String name){
        super("auction_" + location.getWorld().getName() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ(), "auctions", false, Pirates.plugin);

        this.name = name;
        this.location = location;
        this.sign = (Sign) location.getBlock().getState();

        AuctionManager.AUCTIONS.put(name, this);

        getFile().set("Name", name);
        super.save();

        getSign().getPersistentDataContainer().set(AuctionManager.AUCTION_KEY, PersistentDataType.BYTE, (byte) 1);
    }

    @Override
    public void save() {
        getFile().set("Name", name);

        String owner = null;
        String bidder = null;

        try {
            owner = getOwner().getBase().toString();
            bidder = getHighestBidder().getBase().toString();
        } catch (Exception ignored){ }

        getFile().set("Item", getItem());
        getFile().set("HighestBid", getHighestBid());
        getFile().set("BaseBid", getBaseBid());
        getFile().set("Expires", getExpiresAt());

        getFile().set("Owner", owner);
        getFile().set("HighestBidder", bidder);

        removeDisplay();

        if(bids != null && !bids.isEmpty()){
            Map<String, Integer> temp = new HashMap<>();
            for (UUID id: bids.keySet()){
                temp.put(id.toString(), bids.get(id));
            }
            getFile().createSection("Bids", temp);
        } else getFile().set("Bids", null);

        super.save();
    }

    @Override
    public void reload() {
        super.reload();
        name = getFile().getString("Name");
        String owner = getFile().getString("Owner");

        if(owner == null){
            unClaim();
            return;
        } else {
            this.owner = FtcCore.getUser(UUID.fromString(owner));

            String highestBidder = getFile().getString("HighestBidder");
            if(highestBidder == null){
                unClaim();
                return;
            }
            else this.highestBidder = FtcCore.getUser(UUID.fromString(highestBidder));

            setItem(getFile().getItemStack("Item"));
            setBaseBid(getFile().getInt("BaseBid"));
            setHighestBid(getFile().getInt("HighestBid"));
            expiresAt = getFile().getLong("Expires");

            createDisplay();
        }

        ConfigurationSection section = getFile().getConfigurationSection("Bids");
        if(section != null){
            Map<UUID, Integer> tempMap = new HashMap<>();

            for (String s: section.getKeys(false)){
                tempMap.put(UUID.fromString(s), section.getInt(s));
            }

            bids = tempMap;
        } else bids = null;

        performExpiryCheck();
        updateSign();
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public void delete() {
        if(isClaimed()){
            getLocation().getWorld().dropItemNaturally(getLocation(), getItem());
            giveBalancesToLosers(true);
        }

        Pirates.getAuctionManager().removeAuction(this);
        super.delete();
    }

    @Override
    public void createDisplay(){
        if(getItem() == null || getItem().getType() == Material.AIR) return;

        WallSign signData = (WallSign) getSign().getBlockData();
        BlockFace attached = signData.getFacing().getOppositeFace();
        Block blockAttached = getSign().getBlock().getRelative(attached); // idk test if this works lol

        Location spawnLoc = blockAttached.getLocation().add(0.5, -0.75, 0.5); // middle of block

        spawnLoc.setYaw(90f);

        ArmorStand stand = (ArmorStand) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);

        stand.setInvisible(true);
        stand.setInvulnerable(true);
        stand.setGravity(false);
        stand.setPersistent(true);
        stand.setRemoveWhenFarAway(false);

        stand.getEquipment().setHelmet(getItem().clone());
        displayEntity = stand;
    }

    @Override
    public Entity getDisplayEntity() {
        return displayEntity;
    }

    @Override
    public boolean isClaimed(){
        return owner != null;
    }

    @Override
    public void setClaimed(CrownUser owner, int baseBid, ItemStack item, boolean admin){
        this.owner = owner;
        this.highestBidder = owner;
        this.baseBid = baseBid;
        this.highestBid = baseBid;
        this.item = item;
        this.adminAuction = admin;
        this.bids = new HashMap<>();

        expiresAt = System.currentTimeMillis() + AuctionManager.EXPIRY_DURATION;

        getSign().line(1, Component.text(item.getAmount()));
        getSign().line(2, Component.text(CrownUtils.getItemNormalName(item)));

        updateSign();
        createDisplay();
    }

    @Override
    public void attemptItemClaim(CrownUser user) throws CrownException{
        if(!highestBidder.equals(user)) throw new CrownException(user, "You cannot claim this item!");
        else {
            if (user.getPlayer().getInventory().firstEmpty() == -1) throw new CrownException(user, "Your inventory is full!");

            Balances bals = FtcCore.getBalances();

            if(!highestBidder.equals(owner)){
                if (bals.get(user.getBase()) < getHighestBid()) throw new CrownException(user, "You do not have enough money to claim the item. Come back when you're a little mmm richer");
                if(bals.get(getOwner().getBase()) < getHighestBid()) throw new CrownException(user, "The owner of the auction cannot afford that!");

                bals.add(user.getBase(), -getHighestBid(), false);
                bals.add(owner.getBase(), getHighestBid(), false);
                owner.sendMessage("&6$ &7You've received &e" + CrownUtils.decimalizeNumber(getHighestBid()) + " Rhines &7from &e" + getName() + "&7 by &e" + user.getName());
            }

            user.getPlayer().getInventory().addItem(getItem());
            user.sendMessage("&eYou've got your item! :D");
        }
        unClaim();
        save();
    }

    @Override
    public void updateSign(){
        if(isClaimed()){
            if(isWaitingForItemClaim()){
                getSign().line(0, AuctionManager.WAITING_FOR_ITEM_CLAIM_LABEL);
            } else {
                if(adminAuction) getSign().line(0, AuctionManager.CLAIMED_ADMIN_LABEL);
                else getSign().line(0, AuctionManager.CLAIMED_REGULAR_LABEL);
            }
            getSign().line(3, AuctionManager.getPriceLine(highestBid));
        } else {
            getSign().line(0, AuctionManager.UNCLAIMED_LABEL);
            getSign().line(3, AuctionManager.getPriceLine(null));
        }
        getSign().update();
    }

    @Override
    public void unClaim(){
        owner = null;
        highestBidder = null;
        baseBid = -1;
        highestBid = baseBid;
        expiresAt = -1;
        waitingForItemClaim = false;
        bids = null;

        getSign().line(1, Component.text(""));
        getSign().line(2, Component.text(""));
        removeDisplay();

        updateSign();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Sign getSign() {
        return sign;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public CrownUser getHighestBidder() {
        return highestBidder;
    }

    @Override
    public void setHighestBidder(CrownUser highestBidder) {
        this.highestBidder = highestBidder;
    }

    @Override
    public void removeDisplay(){
        if(displayEntity != null){
            displayEntity.remove();
            return;
        }

        for (Entity e: getLocation().getNearbyEntities(1, 1, 1)){
            if(!(e instanceof ArmorStand)) continue;
            e.remove();
        }
    }

    @Override
    public CrownUser getOwner() {
        return owner;
    }

    @Override
    public void setOwner(CrownUser owner) {
        this.owner = owner;
    }

    @Override
    public int getHighestBid() {
        return highestBid;
    }

    @Override
    public void setHighestBid(int highestBid) {
        this.highestBid = highestBid;
        updateSign();
    }

    @Override
    public void bidOn(CrownUser user, int value) {
        if(!getHighestBidder().equals(user))
            getHighestBidder().sendMessage("&7You've been outbid on &e" + getName() + " &7by &e" + user.getName());

        setHighestBidder(user);
        setHighestBid(value);

        if(bids == null) bids = new HashMap<>();

        if(bids.containsKey(user.getBase())) value = value - bids.get(user.getBase());
        FtcCore.getBalances().add(user.getBase(), -value);

        bids.put(user.getBase(), value);
    }

    //true if not expired, false if expired
    @Override
    public boolean performExpiryCheck(){
        if(!isClaimed()) return true;
        if(isWaitingForItemClaim()) return false;
        if(System.currentTimeMillis() > expiresAt){
            setWaitingForItemClaim(true);
            getSign().line(0, AuctionManager.WAITING_FOR_ITEM_CLAIM_LABEL);
            updateSign();
            giveBalancesToLosers(false);
            return false;
        }
        return true;
    }

    @Override
    public void giveBalancesToLosers(boolean toHighestBidder){
        if(bids == null) return;

        Balances bals = FtcCore.getBalances();
        for (UUID id: bids.keySet()){
            if(!toHighestBidder && id.equals(getHighestBidder().getBase())) continue;

            int amount = bids.get(id);

            FtcCore.getUser(id).sendMessage("&6$ &7You received &e" + CrownUtils.decimalizeNumber(amount) + " Rhines&7 from your bid on &e" + getName());
            bals.add(id, amount);
        }
    }

    @Override
    public boolean isWaitingForItemClaim() {
        return waitingForItemClaim;
    }

    @Override
    public void setWaitingForItemClaim(boolean waitingForItemClaim) {
        this.waitingForItemClaim = waitingForItemClaim;
    }

    @Override
    public int getBaseBid() {
        return baseBid;
    }

    @Override
    public void setBaseBid(int baseBid) {
        this.baseBid = baseBid;
    }

    @Override
    public ItemStack getItem() {
        try {
            return item.clone();
        } catch (Exception e){
            return null;
        }
    }

    @Override
    public void setItem(ItemStack item) {
        this.item = item;
    }

    @Override
    public long getExpiresAt() {
        return expiresAt;
    }

    @Override
    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public boolean isAdminAuction() {
        return adminAuction;
    }

    @Override
    public void setAdminAuction(boolean adminAuction) {
        this.adminAuction = adminAuction;
    }

    @Override
    public void setBids(Map<UUID, Integer> bids) {
        this.bids = bids;
    }

    @Override
    public Map<UUID, Integer> getBids() {
        return bids;
    }
}
