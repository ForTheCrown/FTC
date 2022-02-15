package net.forthecrown.dungeons;

import com.google.gson.JsonArray;
import net.forthecrown.core.Keys;
import net.forthecrown.dungeons.boss.KeyedBoss;
import net.forthecrown.user.BooleanDataAccessor;
import net.forthecrown.user.UserDataContainer;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public class DungeonUserDataAccessor implements BooleanDataAccessor<KeyedBoss> {
    private static final Key ACCESS_KEY = Keys.forthecrown("dungeon_data");

    @Override
    public boolean getStatus(UserDataContainer c, KeyedBoss val) {
        return get(c).contains(val.serialize());
    }

    @Override
    public void setStatus(UserDataContainer c, KeyedBoss boss, boolean state) {
        JsonArray array = get(c);

        if(state) array.add(boss.serialize());
        else array.remove(boss.serialize());

        set(c, array);
    }

    private JsonArray get(UserDataContainer c) {
        return c.getOrDefault(this, new JsonArray()).getAsJsonArray();
    }

    private void set(UserDataContainer container, JsonArray json) {
        if(json == null || json.isEmpty()) {
            container.remove(this);
        } else container.set(this, json);
    }

    @Override
    public @NotNull Key accessKey() {
        return ACCESS_KEY;
    }
}
