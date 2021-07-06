package net.forthecrown.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.serializer.JsonDeserializable;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.CrownUtils;
import net.kyori.adventure.key.Key;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows other plugins to store data in a user's file.
 */
public class FtcUserDataContainer implements JsonSerializable, JsonDeserializable, UserDataContainer {

    private final Map<Key, JsonElement> data = new HashMap<>();
    private final FtcUser user;

    FtcUserDataContainer(FtcUser user){
        this.user = user;
    }

    @Override
    public void set(Key key, JsonElement section){
        data.put(key, section);
    }

    @Nonnull
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

    @Nonnull
    @Override
    public CrownUser getUser(){
        return user;
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
            data.put(CrownUtils.parseKey(e.getKey()), e.getValue());
        }
    }
}
