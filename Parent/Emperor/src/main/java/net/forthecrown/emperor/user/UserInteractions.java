package net.forthecrown.emperor.user;

import com.mojang.authlib.minecraft.SocialInteractionsService;
import net.forthecrown.emperor.admin.MuteStatus;
import net.forthecrown.emperor.user.data.TeleportRequest;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserInteractions extends SocialInteractionsService {
    MuteStatus muteStatus();

    Set<UUID> getBlockedUsers();
    CrownUser getUser();

    void addBlocked(UUID id);
    void removeBlocked(UUID id);

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

    long getLastMarriageStatusChange();

    void setLastMarriageStatusChange(long lastMarriageStatusChange);

    boolean canChangeMarriageStatus();

    UUID getLastMarriageRequest();

    void setLastMarriageRequest(UUID lastMarriageRequest);

    boolean isAcceptingProposals();

    void setAcceptingProposals(boolean acceptingProposals);

    UUID getWaitingFinish();

    void setWaitingFinish(UUID waitingFinish);

    boolean isMarriageChatToggled();

    void setMarriageChatToggled(boolean marriageChatToggled);
}
