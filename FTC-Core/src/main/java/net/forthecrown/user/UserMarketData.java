package net.forthecrown.user;

import net.forthecrown.core.ComVars;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.TimeUtil;

import java.util.UUID;

public interface UserMarketData extends UserAttachment {
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

    long getGuildJoinDate();
    void setGuildJoinDate(long date);

    void setKickedFromGuild(long date);
    long getKickedFromGuild();

    default boolean affectedByKickCooldown() {
        return !TimeUtil.hasCooldownEnded(ComVars.guildKickSafeTime(), getKickedFromGuild());
    }

    default void setJoinedGuild() {
        setGuildJoinDate(System.currentTimeMillis());
    }

    default boolean hasJoinedGuild() {
        return getGuildJoinDate() != 0;
    }

    default boolean canBeKickedFromGuild() {
        if(!hasJoinedGuild()) return false;
        return TimeUtil.hasCooldownEnded(ComVars.guildKickSafeTime(), getGuildJoinDate());
    }

    default boolean currentlyOwnsShop() {
        return !FtcUtils.isNullOrBlank(getOwnedName());
    }

    default boolean canChangeStatus() {
        return TimeUtil.hasCooldownEnded(ComVars.getMarketStatusCooldown(), getLastStatusChange());
    }

    default void setLastStatusChange() {
        setLastStatusChange(System.currentTimeMillis());
    }
}
