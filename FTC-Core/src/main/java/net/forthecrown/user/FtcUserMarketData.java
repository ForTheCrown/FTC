package net.forthecrown.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.serializer.JsonWrapper;

import java.util.UUID;

public class FtcUserMarketData extends AbstractUserAttachment implements UserMarketData {
    @Getter @Setter
    public String ownedName;

    @Getter @Setter
    private UUID outgoing;

    @Getter @Setter
    public long ownershipBegan, lastStatusChange;

    public long joinDate;
    public long kickDate;

    private final ObjectList<UUID> incoming = new ObjectArrayList<>();

    public FtcUserMarketData(FtcUser user) {
        super(user, "marketOwnership");
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

            user.getMarketData().setOutgoing(null);
            user.unloadIfOffline();
        }

        incoming.clear();
    }

    @Override
    public long getGuildJoinDate() {
        return joinDate;
    }

    @Override
    public void setGuildJoinDate(long date) {
        this.joinDate = date;
    }

    @Override
    public void setKickedFromGuild(long date) {
        this.kickDate = date;
    }

    @Override
    public long getKickedFromGuild() {
        return kickDate;
    }

    @Override
    public void deserialize(JsonElement element) {
        ownershipBegan = 0L;
        joinDate = 0L;
        lastStatusChange = 0L;
        kickDate = 0L;
        ownedName = null;

        if(element == null) return;
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        lastStatusChange = json.getLong("lastAction");
        ownershipBegan = json.getLong("ownershipBegan");
        ownedName = json.getString("ownedName");
        this.joinDate = json.getLong("guildJoinDate");
    }

    @Override
    public JsonObject serialize() {
        JsonWrapper json = JsonWrapper.empty();

        if(lastStatusChange != 0L) json.add("lastAction", lastStatusChange);
        if(ownershipBegan != 0L) json.add("ownershipBegan", ownershipBegan);
        if(currentlyOwnsShop()) json.add("ownedName", ownedName);
        if(hasJoinedGuild()) json.add("guildJoinDate", joinDate);
        if(affectedByKickCooldown()) json.add("guildKickDate", joinDate);

        return json.nullIfEmpty();
    }
}