package net.forthecrown.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.serializer.JsonWrapper;

public class FtcUserMarketOwnership extends AbstractUserAttachment implements UserMarketOwnership {
    public long ownershipBegan;
    public String ownedName;

    public FtcUserMarketOwnership(FtcUser user) {
        super(user);
    }

    @Override
    public long getOwnershipBegan() {
        return ownershipBegan;
    }

    @Override
    public void setOwnershipBegan(long ownershipBegan) {
        this.ownershipBegan = ownershipBegan;
    }

    @Override
    public String getOwnedName() {
        return ownedName;
    }

    @Override
    public void setOwnedName(String ownedName) {
        this.ownedName = ownedName;
    }

    @Override
    public void deserialize(JsonElement element) {
        ownershipBegan = 0L;

        if(element == null) return;
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        ownershipBegan = json.getLong("ownershipBegan");
        ownedName = json.getString("ownedName");
    }

    @Override
    public JsonObject serialize() {
        JsonWrapper json = JsonWrapper.empty();

        if(ownershipBegan != 0L) json.add("ownershipBegan", ownershipBegan);
        if(currentlyOwnsShop()) json.add("ownedName", ownedName);

        return json.nullIfEmpty();
    }
}
