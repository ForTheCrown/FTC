package net.forthecrown.emperor.user;

import net.forthecrown.emperor.Announcer;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.economy.BalanceMap;
import net.forthecrown.emperor.economy.SortedBalanceMap;
import net.forthecrown.emperor.serialization.AbstractSerializer;
import net.forthecrown.emperor.utils.MapUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public final class CrownUserManager extends AbstractSerializer<CrownCore> implements UserManager {

    private Map<UUID, UUID> alts = new HashMap<>();
    private final CrownCore core;

    public CrownUserManager(CrownCore core){
        super("usermanager", core);

        this.core = core;
        reload();
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
        Announcer.log(Level.INFO, s);
    }

    public void checkAllUserDatas(){
        File file = new File(core.getDataFolder().getPath() + File.separator + "playerdata");
        if(!file.isDirectory()) return;

        int amount = 0;
        for (File f: file.listFiles()){
            FileConfiguration config = YamlConfiguration.loadConfiguration(f);
            if(config.getLong("TimeStamps.LastLoad") == 0){

                config.set("TimeStamps.LastLoad", System.currentTimeMillis());
                try {
                    config.save(f);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                continue;
            }

            if((System.currentTimeMillis() - config.getLong("TimeStamps.LastLoad") > CrownCore.getUserDataResetInterval())){
                if(!f.delete()) CrownCore.inst().getLogger().log(Level.WARNING, "Couldn't delete file named " + f.getName());
                else log("Deleted file of user " + f.getName() + ". File was last loaded more than 2 months ago");
                resetBalance(f.getName().replaceAll(".yml", ""));

                amount++;
            }
        }
        log("All user data files have been checked for deletion. Deleted " + amount + " files.");
    }

    private void resetBalance(String toUUID){
        UUID id = UUID.fromString(toUUID);
        BalanceMap map = CrownCore.getBalances().getMap();
        map.remove(id);
        CrownCore.getBalances().setMap((SortedBalanceMap) map);
    }

}