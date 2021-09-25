package net.forthecrown.user;

import net.forthecrown.utils.FtcUtils;

public interface UserMarketOwnership extends UserAttachment {
    long getOwnershipBegan();
    void setOwnershipBegan(long ownershipBegan);

    default boolean hasOwnedBefore() {
        return getOwnershipBegan() != 0L;
    }

    String getOwnedName();
    void setOwnedName(String name);

    default boolean currentlyOwnsShop() {
        return !FtcUtils.isNullOrBlank(getOwnedName());
    }
}
