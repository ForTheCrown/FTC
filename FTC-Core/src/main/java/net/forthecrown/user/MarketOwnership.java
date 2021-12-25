package net.forthecrown.user;

import net.forthecrown.core.ComVars;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.TimeUtil;

import java.util.UUID;

public interface MarketOwnership extends UserAttachment {
    long getOwnershipBegan();
    void setOwnershipBegan(long ownershipBegan);

    long getLastStatusChange();
    void setLastStatusChange(long statusChange);

    default boolean hasOwnedBefore() {
        return getOwnershipBegan() != 0L;
    }

    String getOwnedName();
    void setOwnedName(String name);

    UUID getOutgoing();
    void setOutgoing(UUID id);

    void addIncoming(UUID sender);
    void removeIncoming(UUID sender);
    boolean hasIncoming(UUID sender);

    void clearIncoming();

    default boolean currentlyOwnsShop() {
        return !FtcUtils.isNullOrBlank(getOwnedName());
    }

    default boolean canChangeStatus() {
        return TimeUtil.hasCooldownEnded(ComVars.getMarketStatusCooldown(), getLastStatusChange());
    }
}
