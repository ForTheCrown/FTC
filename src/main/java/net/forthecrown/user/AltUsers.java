package net.forthecrown.user;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.SerializableObject;
import org.bukkit.entity.Player;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AltUsers extends SerializableObject.Json {
    private final Map<UUID, UUID> alt2Main = new Object2ObjectOpenHashMap<>();
    private final UserManager manager;

    public AltUsers(Path filePath, UserManager manager) {
        super(filePath);
        this.manager = manager;
    }

    public void save(JsonWrapper json) {
        for (var e: alt2Main.entrySet()) {
            json.add(
                    e.getKey().toString(),
                    e.getValue().toString()
            );
        }
    }

    public void load(JsonWrapper json) {
        alt2Main.clear();

        for (var e: json.entrySet()) {
            addEntry(
                    UUID.fromString(e.getKey()),
                    UUID.fromString(e.getValue().getAsString())
            );
        }
    }

    public UUID getMain(UUID id){
        return alt2Main.get(id);
    }

    public boolean isAlt(UUID id){
        return alt2Main.containsKey(id);
    }

    public boolean isAltForAny(UUID id, Collection<Player> players){
        UUID main = getMain(id);

        if (main == null) {
            return false;
        }

        for (Player p: players) {
            if (main.equals(p.getUniqueId())) {
                return true;
            }
        }

        return false;
    }

    public boolean isMainForAny(UUID id, Collection<Player> players){
        List<UUID> alts = getAlts(id);

        for (Player p: players){
            if (alts.contains(p.getUniqueId())) {
                return true;
            }
        }

        return false;
    }

    public List<UUID> getAlts(UUID main){
        List<UUID> list = new ObjectArrayList<>();

        for (Map.Entry<UUID, UUID> entry: alt2Main.entrySet()){
            if (!entry.getValue().equals(main)) {
                continue;
            }

            list.add(entry.getKey());
        }
        return list;
    }

    public void addEntry(UUID alt, UUID main){
        alt2Main.put(alt, main);

        var user = Users.getLoadedUser(alt);

        // If user is offline or not loaded... for some reason
        // Don't do stuff
        if (user == null || !user.isOnline()) {
            return;
        }

        // Unload user and replace entry with alt
        // (getUser automatically returns an alt user
        //  if the user is an alt)
        manager.unload(user);
        Users.get(alt);
    }

    public void removeEntry(UUID alt) {
        alt2Main.remove(alt);

        // Same logic as addEntry(UUID, UUID),
        // except with LOADED_ALTS this time
        var user = Users.getLoadedUser(alt);
        if (user == null || !user.isOnline()) {
            return;
        }

        manager.unload(user);
        Users.get(alt);
    }
}