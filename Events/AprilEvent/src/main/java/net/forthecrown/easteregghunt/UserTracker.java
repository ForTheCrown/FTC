package net.forthecrown.easteregghunt;

import net.forthecrown.core.comvars.ComVar;
import net.forthecrown.core.comvars.ComVars;
import net.forthecrown.core.comvars.types.ComVarType;
import net.forthecrown.core.files.AbstractSerializer;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.utils.MapUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserTracker extends AbstractSerializer<EasterMain> implements IUserTracker {

    private Map<UUID, Byte> tracker = new HashMap<>();
    private byte day = 0;
    private BukkitRunnable runnable;
    public static final ComVar<Byte> delayTime = ComVars.set("ev_clearInterval", ComVarType.BYTE, (byte) 15);
    public static final ComVar<Byte> maxGoes = ComVars.set("ev_maxAttempts", ComVarType.BYTE, (byte) 5);

    public UserTracker() {
        super("tracker", EasterMain.inst);
        reload();

        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                clear();
            }
        };
        runnable.runTaskTimer(EasterMain.inst, delayTime.getValue()*60*20, delayTime.getValue()*60*20);
        delayTime.setOnUpdate(aByte -> {
            runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    clear();
                }
            };
            runnable.runTaskTimer(EasterMain.inst, aByte*60*20, aByte*60*20);
        });
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
        return !(tracker.get(player.getUniqueId()) >= maxGoes.getValue((byte) 5));
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
