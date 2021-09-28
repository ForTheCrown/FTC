package net.forthecrown.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.manager.UserManager;

import java.util.UUID;

public class FtcMarketOwnership extends AbstractUserAttachment implements MarketOwnership {
    public long ownershipBegan;
    public long lastAction;
    public String ownedName;

    private final ObjectList<UUID> incoming = new ObjectArrayList<>();
    private UUID outgoing;

    public FtcMarketOwnership(FtcUser user) {
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
    public long getLastStatusChange() {
        return lastAction;
    }

    @Override
    public void setLastStatusChange(long statusChange) {
        lastAction = statusChange;
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
    public UUID getOutgoing() {
        return outgoing;
    }

    @Override
    public void setOutgoing(UUID id) {
        this.outgoing = id;
    }

    @Override
    public void addIncoming(UUID sender) {
        incoming.add(sender);
    }

    @Override
    public void removeIncoming(UUID sender) {
        incoming.remove(sender);
    }

    @Override
    public boolean hasIncoming(UUID sender) {
        return incoming.contains(sender);
    }

    @Override
    public void clearIncoming() {
        for (UUID id: incoming) {
            CrownUser user = UserManager.getUser(id);

            user.getMarketOwnership().setOutgoing(null);
            user.unloadIfOffline();
        }

        incoming.clear();
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
