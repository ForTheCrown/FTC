package net.forthecrown.pirates.auctions;

import net.forthecrown.core.api.CrownSerializer;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.pirates.Pirates;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public interface Auction extends CrownSerializer<Pirates> {

    void delete();

    void createDisplay();

    Entity getDisplayEntity();

    boolean isClaimed();

    void setClaimed(CrownUser owner, int baseBid, ItemStack item, boolean admin);

    void attemptItemClaim(CrownUser user) throws CrownException;

    void updateSign();

    void unClaim();

    void save();

    void reload();

    String getName();

    Sign getSign();

    Location getLocation();

    CrownUser getHighestBidder();

    void setHighestBidder(CrownUser highestBidder);

    void removeDisplay();

    CrownUser getOwner();

    void setOwner(CrownUser owner);

    int getHighestBid();

    void setHighestBid(int highestBid);

    void bidOn(CrownUser user, int value);

    //true if not expired, false if expired
    boolean performExpiryCheck();

    void giveBalancesToLosers(boolean toHighestBidder);

    boolean isWaitingForItemClaim();

    void setWaitingForItemClaim(boolean waitingForItemClaim);

    int getBaseBid();

    void setBaseBid(int baseBid);

    ItemStack getItem();

    void setItem(ItemStack item);

    long getExpiresAt();

    void setExpiresAt(long expiresAt);

    boolean isAdminAuction();

    void setAdminAuction(boolean adminAuction);

    void setBids(Map<UUID, Integer> bids);

    Map<UUID, Integer> getBids();

    boolean isDeleted();

    void setFreeForAll(long freeForAll);

    long getFreeForAll();
}
