package net.forthecrown.core.files;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.CrownUserAlt;

import java.util.UUID;

public class FtcUserAlt extends FtcUser implements CrownUserAlt {

    private final UUID mainID;
    private final CrownUser main;

    public FtcUserAlt(UUID base, UUID main){
        super(base);
        this.mainID = main;
        this.main = FtcCore.getUser(main);
    }

    @Override
    public void save() {
    }

    @Override
    public void reload() {
    }

    @Override
    public UUID getMainUniqueID() {
        return mainID;
    }

    @Override
    public CrownUser getMain() {
        return main;
    }
}
