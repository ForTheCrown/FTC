package net.forthecrown.vikings.blessings;

import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.serialization.AbstractSerializer;
import net.forthecrown.vikings.Vikings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.*;

public abstract class VikingBlessing extends AbstractSerializer<Vikings> implements Listener {
    private static final Set<VikingBlessing> BLESSINGS = new HashSet<>();
    protected final String name;

    private final Set<UUID> currently_using = new HashSet<>(); //name lol
    private final Set<UUID> temp_users = new HashSet<>();

    protected VikingBlessing(String name, Vikings plugin){
        super(name, "vikingblessings", plugin);

        this.name = name;
        BLESSINGS.add(this);

        reload();
    }

    @Override
    public void saveFile() {
        List<String> temp = new ArrayList<>();
        for (UUID id: currently_using){
            temp.add(id.toString());
        }

        getFile().set("players", temp);
    }

    @Override
    public void reloadFile() {
        currently_using.clear();
        for (String s: getFile().getStringList("players")){
            try {
                currently_using.add(UUID.fromString(s));
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
        currently_using.add(user.getUniqueId());
        onPlayerEquip(user);
    }

    public final void beginTempUsage(CrownUser user, int expiresInTicks){
        temp_users.add(user.getUniqueId());
        onPlayerEquip(user);

        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> endUsage(user), expiresInTicks);
    }

    public final void endUsage(CrownUser user){
        currently_using.remove(user.getUniqueId());
        temp_users.remove(user.getUniqueId());
        onPlayerUnequip(user);
    }

    public void clearTempUsers(){
        for (UUID id: temp_users){
            temp_users.remove(id);

            Player player = Bukkit.getPlayer(id);
            if(player == null) continue;

            onPlayerUnequip(UserManager.getUser(player));
        }
    }

    public Set<UUID> getUsers(){
        return currently_using;
    }

    public Set<UUID> getTempUsers(){
        return temp_users;
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
