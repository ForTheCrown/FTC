package net.forthecrown.core.files;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.utils.MapUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public final class CrownUserManager extends AbstractSerializer<FtcCore> implements UserManager {

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

        ConfigurationSection section = getFile().getConfigurationSection("Alts");
        if(section == null){
            save();
            return;
        }
        alts = MapUtils.convert(section.getValues(false), UUID::fromString, obj -> UUID.fromString(obj.toString()));
    }

    @Override
    public void saveUsers(){
        LOADED_USERS.values().forEach(FtcUser::save);
    }

    @Override
    public void reloadUsers(){
        LOADED_USERS.values().forEach(FtcUser::reload);
    }

    @Override
    public UUID getMain(UUID id){
        if(!alts.containsKey(id)) return null;
        return alts.get(id);
    }

    @Override
    public boolean isAlt(UUID id){
        return alts.containsKey(id);
    }

    @Override
    public boolean isAltForAny(UUID id, Collection<Player> players){
        UUID main = getMain(id);
        if(main == null) return false;

        for (Player p: players){
            if(main.equals(p.getUniqueId())) return true;
        }
        return false;
    }

    @Override
    public boolean isMainForAny(UUID id, Collection<Player> players){
        List<UUID> id_alts = getAlts(id);

        for (Player p: players){
            if(id_alts.contains(p.getUniqueId())) return true;
        }
        return false;
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
    public void addEntry(UUID alt, UUID main){
        alts.put(alt, main);
    }

    @Override
    public void removeEntry(UUID alt){
        alts.remove(alt);
    }

}