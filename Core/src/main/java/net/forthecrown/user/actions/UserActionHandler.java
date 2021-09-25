package net.forthecrown.user.actions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Crown;

public interface UserActionHandler {

    /**
     * Handles any initial actions sent to this handler
     * @param interaction The interaction to handle
     */
    default void handle(UserAction interaction) {
        interaction.handle(this);
    }

    /**
     * Allows for quick and easy handling of actions
     * @param action The action to handle
     */
    static void handleAction(UserAction action) {
        Crown.getUserManager().getActionHandler().handle(action);
    }

    /**
     * Handles a direct message from one command source to another
     * @see DirectMessage
     * @param message The message to handle
     */
    void handleDirectMessage(DirectMessage message);

    /**
     * Handles a message sent from one user to their spouse
     * @see MarriageMessage
     * @param message The message to handle
     */
    void handleMarriageMessage(MarriageMessage message);

    /**
     * Handles a user's invite to another user to visit their home region
     * @see InviteToRegion
     * @param invite The invite to handle
     */
    void handleRegionInvite(InviteToRegion invite);

    /**
     * Handles a teleport request from one user to another
     * @param request The request to handle
     */
    void handleTeleport(TeleportRequest request);

    void handleMailQuery(MailQuery query) throws CommandSyntaxException;

    void handleMailAdd(MailAddAction action) throws CommandSyntaxException;

    /**
     * Handles a region visit
     * @param visit The visit to handle
     */
    void handleVisit(RegionVisit visit);
}
