package net.forthecrown.user.actions;

import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.Struct;

/**
 * struct for an invite from one user to another
 */
public record InviteToRegion(CrownUser sender,
                             CrownUser target)
        implements UserAction, Struct
{
    @Override
    public void handle(UserActionHandler handler) {
        handler.handleRegionInvite(this);
    }
}
