package net.forthecrown.user;

import java.util.UUID;

public class UserAlt extends User {
    private UUID mainID;
    private User main;

    public UserAlt(UUID base, UUID main) {
        super(base);
        this.mainID = main;
        this.main = Users.get(main);
    }

    public UUID getMainUniqueID() {
        return mainID == null ? mainID = UserManager.get().getAlts().getMain(getUniqueId()) : mainID;
    }

    public User getMain() {
        return main == null ? main = Users.get(getMainUniqueID()) : main;
    }
}