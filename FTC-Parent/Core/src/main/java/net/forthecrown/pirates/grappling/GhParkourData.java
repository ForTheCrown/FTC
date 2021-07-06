package net.forthecrown.pirates.grappling;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.serializer.AbstractJsonSerializer;

import java.util.*;

public class GhParkourData extends AbstractJsonSerializer {

    private final Map<String, GhLevelData> levels = new HashMap<>();

    protected GhParkourData() {
        super("parkour_data");

        reload();
    }

    public GhLevelData get(String s){
        return levels.get(s);
    }

    public void set(String s, GhLevelData data){
        levels.put(s, data);
    }

    public void removeAllFor(UUID id){
        levels.values().forEach(data -> data.removeCompleted(id));
    }

    public Set<String> keySet() {
        return levels.keySet();
    }

    public Collection<GhLevelData> values() {
        return levels.values();
    }

    public Set<Map.Entry<String, GhLevelData>> entrySet() {
        return levels.entrySet();
    }

    @Override
    protected void save(JsonObject json) {
        levels.values().forEach(d -> json.add(d.getName(), d.serialize()));
    }

    @Override
    protected void reload(JsonObject json) {
        levels.clear();

        for (Map.Entry<String, JsonElement> e: json.entrySet()){
            levels.put(e.getKey(), new GhLevelData(e.getKey(), e.getValue()));
        }
    }

    @Override
    protected JsonObject createDefaults(JsonObject json) {
        

        return json;
    }
}
