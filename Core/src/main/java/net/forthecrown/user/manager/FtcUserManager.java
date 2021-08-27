package net.forthecrown.user.manager;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.Crown;
import net.forthecrown.serializer.AbstractYamlSerializer;
import net.forthecrown.user.FtcUser;
import net.forthecrown.user.FtcUserAlt;
import net.forthecrown.user.actions.FtcUserActionHandler;
import net.forthecrown.user.actions.UserActionHandler;
import net.forthecrown.utils.MapUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public final class FtcUserManager extends AbstractYamlSerializer implements UserManager {

    public static final Object2ObjectMap<UUID, FtcUser> LOADED_USERS = new Object2ObjectOpenHashMap<>();
    public static final Object2ObjectMap<UUID, FtcUserAlt> LOADED_ALTS = new Object2ObjectOpenHashMap<>();

    private final FtcUserActionHandler actionHandler = new FtcUserActionHandler();

    private Map<UUID, UUID> alts = new Object2ObjectOpenHashMap<>();

    public FtcUserManager(){
        super("usermanager");

        reload();
        Crown.logger().info("User manager loaded");
    }

    @Override
    public void saveFile() {
        getFile().createSection("Alts", MapUtils.convert(alts, UUID::toString, UUID::toString));
    }

    @Override
    public void reloadFile() {
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
        LOADED_USERS.entrySet().removeIf(entry -> !entry.getValue().isOnline());
    }

    @Override
    public void reloadUsers(){
        LOADED_USERS.values().forEach(FtcUser::reload);
    }

    @Override
    public UUID getMain(UUID id){
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

    @Override
    public UserActionHandler getActionHandler() {
        return actionHandler;
    }
}