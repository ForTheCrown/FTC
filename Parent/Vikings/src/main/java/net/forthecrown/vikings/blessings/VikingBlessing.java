package net.forthecrown.vikings.blessings;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.files.AbstractSerializer;
import net.forthecrown.vikings.Vikings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.*;

public abstract class VikingBlessing extends AbstractSerializer<Vikings> implements Listener {
    private static final Set<VikingBlessing> BLESSINGS = new HashSet<>();
    protected final String name;

    private final Set<UUID> usingUsers = new HashSet<>(); //name lol
    private final Set<UUID> tempUsers = new HashSet<>();

    protected VikingBlessing(String name, Vikings plugin){
        super(name, "vikingblessings", plugin);

        this.name = name;
        BLESSINGS.add(this);

        reload();
    }

    @Override
    public void save() {
        List<String> temp = new ArrayList<>();
        for (UUID id: usingUsers){
            temp.add(id.toString());
        }

        getFile().set("players", temp);
        super.save();
    }

    @Override
    public void reload() {
        super.reload();

        usingUsers.clear();
        for (String s: getFile().getStringList("players")){
            try {
                usingUsers.add(UUID.fromString(s));
            } catch (Exception ignored) {}
        }
    }

    protected abstract void onPlayerEquip(CrownUser user);
    protected abstract void onPlayerUnequip(CrownUser user);

    public String getName(){
        return name;
    }

    public void registerEvents(){
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    }

    public void unregisterEvents(){
        HandlerList.unregisterAll(this);
    }

    public final void beginUsage(CrownUser user){
        usingUsers.add(user.getBase());
        onPlayerEquip(user);
    }

    public final void beginTempUsage(CrownUser user, int expiresInTicks){
        tempUsers.add(user.getBase());
        onPlayerEquip(user);

        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> endUsage(user), expiresInTicks);
    }

    public final void endUsage(CrownUser user){
        usingUsers.remove(user.getBase());
        tempUsers.remove(user.getBase());
        onPlayerUnequip(user);
    }

    public void clearTempUsers(){
        for (UUID id: tempUsers){
            tempUsers.remove(id);

            Player player = Bukkit.getPlayer(id);
            if(player == null) continue;

            onPlayerUnequip(FtcCore.getUser(player));
        }
    }

    public Set<UUID> getUsers(){
        return usingUsers;
    }

    public Set<UUID> getTempUsers(){
        return tempUsers;
    }

    @Override
    public String toString() {
        return name;
    }

    public static VikingBlessing fromName(String name){
        for (VikingBlessing b: getBlessings()){
            if(b.getName().equalsIgnoreCase(name)) return b;
        }
        return null;
    }

    public static Set<VikingBlessing> getBlessings(){
        return BLESSINGS;
    }
}
