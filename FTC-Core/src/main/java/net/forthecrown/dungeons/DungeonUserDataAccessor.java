package net.forthecrown.dungeons;

import com.google.gson.JsonArray;
import net.forthecrown.core.Keys;
import net.forthecrown.dungeons.bosses.DungeonBoss;
import net.forthecrown.user.data.BooleanDataAccessor;
import net.forthecrown.user.data.UserDataContainer;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public class DungeonUserDataAccessor implements BooleanDataAccessor<DungeonBoss> {
    private static final Key ACCESS_KEY = Keys.forthecrown("dungeon_data");

    @Override
    public boolean getStatus(UserDataContainer c, DungeonBoss val) {
        return get(c).contains(val.serialize());
    }

    @Override
    public void setStatus(UserDataContainer c, DungeonBoss boss, boolean state) {
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
