package net.forthecrown.core.files;

import net.forthecrown.core.api.Announcer;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.CrownUserAlt;
import net.forthecrown.core.api.UserManager;

import java.util.UUID;

public class FtcUserAlt extends FtcUser implements CrownUserAlt {

    private UUID mainID = null;
    private CrownUser main = null;

    public FtcUserAlt(UUID base, UUID main){
        super(base);
        this.mainID = main;
        this.main = UserManager.getUser(main);
        CrownUserManager.LOADED_ALTS.put(base, this);
    }

    @Override
    public void save() {
    }

    @Override
    public void reload() {
    }

    @Override
    public UUID getMainUniqueID() {
        Announcer.debug("getMainUniqueID called");
        if(mainID == null) mainID = UserManager.inst().getMain(getUniqueId());
        Announcer.debug("result: " + mainID);
        return mainID;
    }

    @Override
    public CrownUser getMain() {
        Announcer.debug("getMain called");
        if(main == null) main = UserManager.getUser(getMainUniqueID());
        Announcer.debug("result: " + main);
        return main;
    }

    @Override
    protected void permsCheck() {

    }

    @Override
    protected boolean shouldResetEarnings() {
        return System.currentTimeMillis() > getMain().getNextResetTime();
    }
}
