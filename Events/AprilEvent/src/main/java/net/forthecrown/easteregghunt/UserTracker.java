package net.forthecrown.easteregghunt;

import net.forthecrown.core.files.AbstractSerializer;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.utils.MapUtils;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserTracker extends AbstractSerializer<EasterMain> implements IUserTracker {

    private Map<UUID, Byte> tracker = new HashMap<>();
    private byte day = 0;

    public UserTracker() {
        super("tracker", EasterMain.inst);

        reload();
    }

    @Override
    public void reload() {
        super.reload();
        day = (byte) getFile().getInt("day");

        if(day == Calendar.getInstance(CrownUtils.SERVER_TIME_ZONE).get(Calendar.DAY_OF_WEEK)){
            if(getFile().getConfigurationSection("users") == null) getFile().createSection("users");
            else {
                HashMap<UUID, Byte> tempMap = new HashMap<>();
                for (String s: getFile().getConfigurationSection("users").getKeys(false)){
                    tempMap.put(UUID.fromString(s), (byte) getFile().getInt("users." + s));
                }
                tracker = tempMap;
            }
        } else tracker.clear();
    }

    @Override
    public Map<UUID, Byte> tracked() {
        return tracker;
    }

    @Override
    public void clear(){
        tracker.clear();
    }

    @Override
    public void save() {
        getFile().set("day", Calendar.getInstance(CrownUtils.SERVER_TIME_ZONE).get(Calendar.DAY_OF_WEEK));
        Map<String, Byte> tempMap = MapUtils.convertKeys(tracker, UUID::toString);
        getFile().createSection("users", tempMap);
        super.save();
    }

    @Override
    public boolean entryAllowed(Player player){
        if(!tracker.containsKey(player.getUniqueId())) return true;
        return !(tracker.get(player.getUniqueId()) >= 5);
    }

    @Override
    public void put(Player player, Byte amount){
        tracker.put(player.getUniqueId(), amount);
    }

    @Override
    public void increment(Player player){
        if(tracker.containsKey(player.getUniqueId())) put(player, (byte) (tracker.get(player.getUniqueId())+1));
        else put(player, (byte) 1);
    }

    @Override
    public byte get(Player player) {
        return tracker.get(player.getUniqueId());
    }
}
