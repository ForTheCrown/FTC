package net.forthecrown.user.actions;

import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.Struct;

/**
 * struct for an invite from one user to another
 */
public class InviteToRegion implements UserAction, Struct {
    private final CrownUser sender;
    private final CrownUser target;

    public InviteToRegion(CrownUser sender, CrownUser target) {
        this.sender = sender;
        this.target = target;
    }

    public CrownUser getSender() {
        return sender;
    }

    public CrownUser getTarget() {
        return target;
    }

    @Override
    public void handle(UserActionHandler handler) {
        handler.handleRegionInvite(this);
    }
}
