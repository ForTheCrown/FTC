package net.forthecrown.core.admin;

import com.google.gson.JsonElement;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.events.dynamic.JailListener;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FtcJailManager extends AbstractJsonSerializer implements JailManager {
    private static final Key KEY = Keys.forthecrown("jails");

    private final Map<Key, Location> jails = new HashMap<>();
    private final Map<Player, JailListener> onlineInJail = new HashMap<>();

    public FtcJailManager(){
        super("jails");
        reload();

        Crown.logger().info("Jails loaded");
    }

    @Override
    protected void save(JsonWrapper json) {
        for (Map.Entry<Key, Location> e: jails.entrySet()){
            json.add(e.getKey().asString(), JsonUtils.writeLocation(e.getValue()));
        }
    }

    @Override
    protected void reload(JsonWrapper json) {
        jails.clear();

        for (Map.Entry<String, JsonElement> e: json.entrySet()){
            Key key = Keys.parse(e.getKey());
            Location loc = JsonUtils.readLocation(e.getValue().getAsJsonObject());

            jails.put(key, loc);
        }
    }

    @NotNull
    @Override
    public Iterator<Location> iterator() {
        return jails.values().iterator();
    }

    @Override
    public Location get(Key key) {
        return jails.get(key);
    }

    @Override
    public Location register(Key key, Location value) {
        jails.put(key, value);
        return value;
    }

    @Override
    public Location remove(Key key) {
        return jails.remove(key);
    }

    @Override
    public Set<Key> keySet() {
        return jails.keySet();
    }

    @Override
    public boolean contains(Key key) {
        return jails.containsKey(key);
    }

    @Override
    public boolean contains(Location value) {
        return jails.containsValue(value);
    }

    @Override
    public Collection<Location> values() {
        return jails.values();
    }

    @Override
    public JailListener getListener(Player player){
        return onlineInJail.get(player);
    }

    @Override
    public void addListener(JailListener listener) {
        onlineInJail.put(listener.player, listener);
    }

    @Override
    public void removeListener(JailListener listener) {
        onlineInJail.remove(listener.player);
    }

    @Override
    public int size() {
        return jails.size();
    }

    @Override
    public void clear() {
        jails.clear();
    }

    @Override
    public boolean isEmpty() {
        return jails.isEmpty();
    }

    @Override
    public Key getKey(Location val) {
        for (Map.Entry<Key, Location> l: jails.entrySet()) {
            if(l.getValue().equals(val)) return l.getKey();
        }

        return null;
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }
}
