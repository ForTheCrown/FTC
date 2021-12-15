package net.forthecrown.user.actions;

import net.forthecrown.user.CrownUser;

public record MarryAction(CrownUser user, CrownUser target,
                          boolean informUsers) implements UserAction {
    @Override
    public void handle(UserActionHandler handler) {
        handler.handleMarry(this);
    }
}
