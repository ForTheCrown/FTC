package net.forthecrown.emperor.admin.jails;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.serialization.AbstractJsonSerializer;
import net.forthecrown.emperor.utils.CrownUtils;
import net.forthecrown.emperor.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CrownJailManager extends AbstractJsonSerializer<CrownCore> implements JailManager {

    private final Map<Key, Location> jails = new HashMap<>();

    public CrownJailManager(){
        super("jails", CrownCore.inst());

        reload();
    }

    @Override
    protected void save(JsonObject json) {
        for (Map.Entry<Key, Location> e: jails.entrySet()){
            json.add(e.getKey().asString(), JsonUtils.serializeLocation(e.getValue()));
        }
    }

    @Override
    protected void reload(JsonObject json) {
        jails.clear();

        for (Map.Entry<String, JsonElement> e: json.entrySet()){
            try {
                Key key = CrownUtils.parseKey(e.getKey());
                Location loc = JsonUtils.deserializeLocation(e.getValue().getAsJsonObject());

                jails.put(key, loc);
            } catch (CommandSyntaxException ignored) {}
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
}
