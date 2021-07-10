package net.forthecrown.user;

import com.mojang.authlib.minecraft.SocialInteractionsService;
import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.user.data.TeleportRequest;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Class representing actions a user can have with another user
 */
public interface UserInteractions extends SocialInteractionsService, UserAttachment {
    MuteStatus muteStatus();

    Set<UUID> getBlockedUsers();

    void addBlocked(UUID id);
    void removeBlocked(UUID id);

    boolean isOnlyBlocked(UUID uuid);
    boolean isSeparatedPlayer(UUID id);

    void addSeparated(UUID uuid);
    void removeSeparated(UUID id);

    void handleTeleport(TeleportRequest request);
    void receiveTeleport(TeleportRequest request);

    void clearIncoming();
    void clearOutgoing();

    TeleportRequest getIncoming(CrownUser user);
    TeleportRequest getOutgoing(CrownUser user);

    List<TeleportRequest> getCurrentOutgoing();
    List<TeleportRequest> getCurrentIncoming();

    void removeIncoming(CrownUser from);
    void removeOutgoing(CrownUser to);

    TeleportRequest firstIncoming();
    TeleportRequest firstOutgoing();

    UUID getMarriedTo();
    void setMarriedTo(UUID marriedTo);

    long getLastMarriageChange();
    void setLastMarriageChange(long lastMarriageChange);

    boolean canChangeMarriageStatus();

    UUID getLastProposal();
    void setLastProposal(UUID lastMarriageRequest);

    boolean acceptingProposals();
    void setAcceptingProposals(boolean acceptingProposals);

    UUID getWaitingFinish();
    void setWaitingFinish(UUID waitingFinish);

    boolean mChatToggled();
    void setMChatToggled(boolean marriageChatToggled);

    default CrownUser marriedToUser(){
        if(getMarriedTo() == null) return null;
        return UserManager.getUser(getMarriedTo());
    }
}
