package net.forthecrown.core.admin.jails;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.events.dynamic.JailListener;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.utils.CrownUtils;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CrownJailManager extends AbstractJsonSerializer implements JailManager {

    private final Map<Key, Location> jails = new HashMap<>();
    private final Map<Player, JailListener> onlineInJail = new HashMap<>();

    public CrownJailManager(){
        super("jails");
        reload();
    }

    @Override
    protected void save(JsonObject json) {
        for (Map.Entry<Key, Location> e: jails.entrySet()){
            json.add(e.getKey().asString(), JsonUtils.writeLocation(e.getValue()));
        }
    }

    @Override
    protected void reload(JsonObject json) {
        jails.clear();

        for (Map.Entry<String, JsonElement> e: json.entrySet()){
            Key key = CrownUtils.parseKey(e.getKey());
            Location loc = JsonUtils.readLocation(e.getValue().getAsJsonObject());

            jails.put(key, loc);
        }
    }

    @Override
    protected JsonObject createDefaults(JsonObject json) {
        return json;
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
    public Location register(Key key, Location raw) {
        jails.put(key, raw);
        return raw;
    }

    @Override
    public void remove(Key key) {
        jails.remove(key);
    }

    @Override
    public Set<Key> getKeys() {
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
    public Collection<Location> getEntries() {
        return jails.values();
    }

    @Override
    public JailListener getJailListener(Player player){
        return onlineInJail.get(player);
    }

    @Override
    public void addJailListener(JailListener listener) {
        onlineInJail.put(listener.player, listener);
    }

    @Override
    public void removeJailListener(JailListener listener) {
        onlineInJail.remove(listener.player);
    }
}
