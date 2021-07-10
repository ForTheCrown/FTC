package net.forthecrown.user;

import net.forthecrown.core.CrownCore;
import net.forthecrown.serializer.AbstractYamlSerializer;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.serializer.UserJsonSerializer;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.MapUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class CrownUserManager extends AbstractYamlSerializer implements UserManager {

    public static final Map<UUID, FtcUser> LOADED_USERS = new HashMap<>();
    public static final Map<UUID, FtcUserAlt> LOADED_ALTS = new HashMap<>();

    private Map<UUID, UUID> alts = new HashMap<>();

    public CrownUserManager(){
        super("usermanager");

        reload();
        CrownCore.logger().info("User manager loaded");
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

    private void log(String s){
        CrownCore.logger().info(s);
    }

    public void runUserDeletionCheck(){
        int amount = 0;
        for (File f: UserJsonSerializer.USER_DIR.listFiles()){
            try {
                JsonBuf json = JsonBuf.of(JsonUtils.readFile(f));
                JsonBuf timeStamps = json.getBuf("timeStamps");

                UUID id;
                try {
                    id = UUID.fromString(f.getName().replaceAll(".json", ""));
                } catch (Exception ignored){
                    continue;
                }

                long lastLoad = timeStamps.getLong("lastLoad");

                if (System.currentTimeMillis() - lastLoad <= CrownCore.getUserResetInterval()) continue;

                //Is older and has been untouched for the entire interval, yeet it
                f.delete();
                CrownCore.getBalances().getMap().remove(id);

                log("Deleting data of " + id);
                amount++;
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        log("All user data files have been checked for deletion. Deleted " + amount + " files.");
    }
}