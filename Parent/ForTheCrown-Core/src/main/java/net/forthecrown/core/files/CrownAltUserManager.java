package net.forthecrown.core.files;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUserAlt;
import net.forthecrown.core.utils.MapUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CrownAltUserManager extends AbstractSerializer<FtcCore> {

    private boolean attemptAutoDetect = false;
    private Map<UUID, UUID> altAndMain = new HashMap<>();
    private Map<UUID, CrownUserAlt> loadedAltUsers = new HashMap<>();

    public CrownAltUserManager() {
        super("altmanager", FtcCore.getInstance());

        reload();
    }

    @Override
    public void reload() {
        super.reload();

        attemptAutoDetect = getFile().getBoolean("AutoDetect");
        altAndMain = MapUtils.convert(getFile().getConfigurationSection("Alts").getValues(false),
                UUID::fromString, obj -> UUID.fromString(obj.toString())
        );
    }

    @Override
    public void save() {
        getFile().createSection("Alts", MapUtils.convert(altAndMain, UUID::toString, UUID::toString));
        getFile().set("AutoDetect", attemptAutoDetect);

        super.save();
    }

    public boolean isAlt(UUID id){
        if(altAndMain.containsKey(id)) return true;
        if(!shouldAttemptAutoDetect()) return false;

        //How
        return false;
    }

    public boolean shouldAttemptAutoDetect(){
        return attemptAutoDetect;
    }

    public boolean hasAlt(UUID id){
        return altAndMain.containsValue(id);
    }

    public CrownUserAlt getAlt(UUID id){
        return null;
    }
}
