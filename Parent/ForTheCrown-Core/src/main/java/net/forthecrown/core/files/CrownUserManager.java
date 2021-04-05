package net.forthecrown.core.files;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.utils.MapUtils;

import java.util.*;

public final class CrownUserManager extends AbstractSerializer<FtcCore> implements UserManager {

    public static final Map<UUID, FtcUser> LOADED_USERS = new HashMap<>();
    public static final Map<UUID, FtcUserAlt> LOADED_ALTS = new HashMap<>();

    private Map<UUID, UUID> alts = new HashMap<>();

    public CrownUserManager(){
        super("usermanager", FtcCore.getInstance());

        reload();
    }

    @Override
    public void save() {
        getFile().createSection("Alts", MapUtils.convert(alts, UUID::toString, UUID::toString));
        super.save();
    }

    @Override
    public void reload() {
        super.reload();
        alts = MapUtils.convert(getFile().getConfigurationSection("Alts").getValues(false), UUID::fromString, obj -> UUID.fromString(obj.toString()));
    }

    @Override
    public void saveUsers(){
        for (FtcUser u: LOADED_USERS.values()){
            u.save();
        }
    }

    @Override
    public void reloadUsers(){
        for (FtcUser u: LOADED_USERS.values()){
            u.reload();
        }
    }

    @Override
    public UUID getMain(UUID id){
        return alts.get(id);
    }

    @Override
    public boolean isAlt(UUID id){
        return getMain(id) != null;
    }

    @Override
    public List<UUID> getAlts(UUID main){
        List<UUID> list = new ArrayList<>();

        for (Map.Entry<UUID, UUID> entry: alts.entrySet()){
            if(!entry.getValue().equals(main)) continue;
            list.add(entry.getKey());
        }
        return list;
    }

    @Override
    public void addAltEntry(UUID alt, UUID main){
        alts.put(alt, main);
    }

    @Override
    public void removeAltEntry(UUID alt){
        alts.remove(alt);
    }

}