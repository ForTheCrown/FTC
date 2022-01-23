package net.forthecrown.user.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.Keys;
import net.forthecrown.user.AbstractUserAttachment;
import net.forthecrown.user.FtcUser;
import net.kyori.adventure.key.Key;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Allows other plugins to store data in a user's file.
 */
public class FtcUserDataContainer extends AbstractUserAttachment implements UserDataContainer {

    private final Map<Key, JsonElement> data = new Object2ObjectOpenHashMap<>();

    public FtcUserDataContainer(FtcUser user){
        super(user, "dataContainer");
    }

    @Override
    public void set(Key key, JsonElement section){
        data.put(key, section);
    }

    @Nullable
    @Override
    public JsonElement get(Key key){
        return data.get(key);
    }

    @Override
    public boolean isEmpty(){
        return data.isEmpty();
    }

    @Override
    public void remove(Key key){
        data.remove(key);
    }

    @Override
    public boolean has(Key key) {
        return data.containsKey(key);
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public JsonObject serialize() {
        if(data.isEmpty()) return null;

        JsonObject json = new JsonObject();

        for (Map.Entry<Key, JsonElement> e: data.entrySet()){
            json.add(e.getKey().asString(), e.getValue());
        }

        return json;
    }

    @Override
    public void deserialize(JsonElement element) {
        data.clear();
        if(element == null) return;
        JsonObject json = element.getAsJsonObject();

        for (Map.Entry<String, JsonElement> e: json.entrySet()){
            data.put(Keys.parse(e.getKey()), e.getValue());
        }
    }
}
