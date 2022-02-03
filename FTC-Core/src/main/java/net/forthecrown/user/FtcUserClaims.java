package net.forthecrown.user;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

public class FtcUserClaims extends AbstractUserAttachment implements UserClaims {
    private final LongSet ownedIds = new LongOpenHashSet();

    public FtcUserClaims(FtcUser user) {
        super(user, "owned_claims");
    }

    @Override
    public void add(long id) {
        ownedIds.add(id);
    }

    @Override
    public void remove(long id) {
        ownedIds.add(id);
    }

    @Override
    public boolean contains(long id) {
        return ownedIds.contains(id);
    }

    @Override
    public int size() {
        return ownedIds.size();
    }

    @Override
    public boolean isEmpty() {
        return ownedIds.isEmpty();
    }

    @Override
    public LongSet getOwned() {
        return ownedIds;
    }

    @Override
    public void deserialize(JsonElement element) {
        ownedIds.clear();

        if(element == null) return;
        JsonArray array = element.getAsJsonArray();

        for (JsonElement e: array) {
            ownedIds.add(e.getAsLong());
        }
    }

    @Override
    public JsonElement serialize() {
        if(ownedIds.isEmpty()) return null;
        JsonArray array = new JsonArray();

        for (long l: ownedIds) {
            array.add(l);
        }

        return array;
    }
}
