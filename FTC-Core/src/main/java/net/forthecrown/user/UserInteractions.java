package net.forthecrown.user;

import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.user.actions.TeleportRequest;
import net.forthecrown.user.manager.UserManager;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Class representing actions a user can have with another user
 */
public interface UserInteractions extends UserAttachment {

    /**
     * Gets the mute status of this user
     * @return The mute status of this user
     */
    MuteStatus muteStatusSilent();

    MuteStatus muteStatus();

    /**
     * Gets all the blocked users of this user
     * @return The user's blocked users
     */
    Set<UUID> getBlockedUsers();

    /**
     * Adds the given UUID to the blocked users list
     * @param id the ID to add
     */
    void addBlocked(UUID id);

    /**
     * Removes the given UUID from the blocked users list
     * @param id The ID to remove
     */
    void removeBlocked(UUID id);

    /**
     * Checks if the given ID is only blocked and not force separated from this user
     * @param uuid The ID to check for
     * @return If the given ID is only blocked by the player
     */
    boolean isOnlyBlocked(UUID uuid);

    boolean chatAllowed();

    boolean isBlockedPlayer(UUID uuid);

    /**
     * Checks if the given ID is force separated from this player
     * @param id The ID to check
     * @return Whether the given ID is separated from this user
     */
    boolean isSeparatedPlayer(UUID id);

    /**
     * Adds an ID to separated players list
     * @param uuid the ID to add
     */
    void addSeparated(UUID uuid);

    /**
     * Removes an ID to separated players list
     * @param id the ID to remove
     */
    void removeSeparated(UUID id);

    /**
     * Adds an outgoing request
     * @param request The request to add
     */
    void addOutgoing(TeleportRequest request);

    /**
     * Adds an incoming teleport request
     * @param request the request to add
     */
    void addIncoming(TeleportRequest request);

    /**
     * Clears all incoming TP requests
     */
    void clearIncoming();

    /**
     * Clears all outgoing TP requests
     */
    void clearOutgoing();

    /**
     * Gets an incoming request from the given user
     * @param user The user to get the request of
     * @return The request sent by the user, or null, if the user hasn't sent a request
     */
    TeleportRequest getIncoming(CrownUser user);

    /**
     * Gets an outgoing request to the given user
     * @param user The user to get the request of
     * @return The request sent to the user, or null, if the user hasn't sent a request
     */
    TeleportRequest getOutgoing(CrownUser user);

    /**
     * Gets all outgoing TP requests
     * @return Outgoing TP requests
     */
    List<TeleportRequest> getCurrentOutgoing();

    /**
     * Gets all incoming TP requests
     * @return Incoming TP requests
     */
    List<TeleportRequest> getCurrentIncoming();

    /**
     * Removes an incoming TP request from the given sender
     * @param from The user that sent the request
     */
    void removeIncoming(CrownUser from);

    /**
     * Removes an outgoing TP request from the given receiver
     * @param to The user that received the request
     */
    void removeOutgoing(CrownUser to);

    /**
     * Gets the first incoming request to this user
     * @return The first incoming request to this user, null, if the user hasn't received any requests
     */
    TeleportRequest firstIncoming();

    /**
     * Gets the first outgoing request from this user
     * @return The first outgoing request from this user, null, if the user hasn't sent any requests
     */
    TeleportRequest firstOutgoing();

    /**
     * Gets the UUID that this user is married to
     * @return The user's spouse's UUID
     */
    UUID getSpouse();

    /**
     * Sets the UUID that this user is married to
     * @param spouse The spouse's UUID
     */
    void setSpouse(UUID spouse);

    /**
     * Gets the last time this user changed their marriage status
     * @return The last marriage change, as a long
     */
    long getLastMarriageChange();

    /**
     * Sets the last time this user changed their marriage status
     * @param lastMarriageChange The last marriage change
     */
    void setLastMarriageChange(long lastMarriageChange);

    /**
     * Checks if this user is allowed to change their marriage status
     * @return ^^^^^^^^^^^^^^^^
     */
    boolean canChangeMarriageStatus();

    /**
     * Gets the last person to have proposed to this user
     * @return the last proposer
     */
    UUID getLastProposal();

    /**
     * Sets the last person to have proposed to this user
     * @param lastMarriageRequest Last marriage request
     */
    void setLastProposal(UUID lastMarriageRequest);

    /**
     * Checks if the user is accepting proposals
     * @return Obvious, innit
     */
    boolean acceptingProposals();

    /**
     * Sets whether the user is accepting proposals or not
     * @param acceptingProposals Obvious, innit
     */
    void setAcceptingProposals(boolean acceptingProposals);

    /**
     * Gets the UUID that's currently waiting Father Ted's blessing to complete the marriage
     * @return The UUID waiting Father Ted's blessing
     */
    UUID getWaitingFinish();

    /**
     * Sets the UUID that's currently waiting to complete the last marriage step
     * @param waitingFinish ^^^^^^
     */
    void setWaitingFinish(UUID waitingFinish);

    /**
     * Checks if the user has marriage chat toggled or not
     * @return ^^^^^
     */
    boolean mChatToggled();

    /**
     * Sets whether the user has marriage chat toggled
     * @param marriageChatToggled Obvious innit
     */
    void setMChatToggled(boolean marriageChatToggled);

    /**
     * Gets the user object of this user's spouse
     * @return The spouse's User, or null, if not married
     */
    default CrownUser spouseUser(){
        if(getSpouse() == null) return null;
        return UserManager.getUser(getSpouse());
    }

    /**
     * Checks if the user has been invited to the given UUID's home region
     * @param by The inviter
     * @return Whether the given UUID invited this user
     */
    boolean hasBeenInvited(UUID by);

    /**
     * Checks whether the user invited the given UUID
     * @param target the invitee
     * @return Whether the given UUID was invited by this user
     */
    boolean hasInvited(UUID target);

    /**
     * Invites the given UUID
     * @param target the UUID to invite
     */
    void addInvite(UUID target);

    /**
     * Invites this user to the given UUID's home region
     * @param sender The invite sender
     */
    void addInvitedTo(UUID sender);

    /**
     * Cancels or just removes an invite to the given UUID
     * @param to the UUID to remove the invite from
     */
    void removeInvite(UUID to);

    /**
     * Removes an invite sent by the given UUID
     * @param from the sender of the invite to remove
     */
    void removeInvitedTo(UUID from);

    /**
     * Checks if the user is married
     * @return true if married, false if not
     */
    default boolean isMarried() {
        return getSpouse() != null;
    }
}
