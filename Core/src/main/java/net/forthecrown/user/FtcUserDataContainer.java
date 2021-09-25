package net.forthecrown.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Allows other plugins to store data in a user's file.
 */
public class FtcUserDataContainer extends AbstractUserAttachment implements UserDataContainer {

    private final Map<Key, JsonElement> data = new Object2ObjectOpenHashMap<>();

    FtcUserDataContainer(FtcUser user){
        super(user);
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
            data.put(FtcUtils.parseKey(e.getKey()), e.getValue());
        }
    }
}
