package net.forthecrown.user.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.user.*;

import java.util.UUID;

/**
 * Stores the data of who wants to merge their
 * shops with this user
 */
public class UserMarketData extends UserComponent {
    /**
     * The ID of the owner of the shop
     * this user last sent a merge
     * request to
     */
    @Getter @Setter
    private transient UUID outgoing;

    /**
     * A list all incoming market merge
     * requests. UUID here represents the
     * owner of the shop that wants to
     * merge
     */
    private transient final ObjectList<UUID> incoming = new ObjectArrayList<>();

    public UserMarketData(User user, ComponentType<UserMarketData> type) {
        super(user, type);
    }

    /**
     * Tests if the given UUID has sent a market merge
     * request to this user
     *
     * @param uuid The uuid to check
     * @return True, if the given user sent a market merge request
     */
    public boolean hasIncoming(UUID uuid) {
        return incoming.contains(uuid);
    }

    /**
     * Adds an incoming market merge request
     * @param uuid The owner of the shop that wants to merge
     */
    public void addIncoming(UUID uuid) {
        incoming.add(uuid);
    }

    /**
     * Removes a market merge request sent by the
     * given user
     * @param uuid The ID of the user that sent the merge request
     */
    public void removeIncoming(UUID uuid) {
        incoming.remove(uuid);
    }

    /**
     * Clears the incoming market merge requests sent to
     * this user.
     * <p>
     * This will also set the {@link #setOutgoing(UUID)} to
     * null on all the users in the incoming list
     */
    public void clearIncoming() {
        for (UUID id: incoming) {
            var user = Users.get(id);

            user.getMarketData().setOutgoing(null);
            user.unloadIfOffline();
        }

        incoming.clear();
    }

    @Override
    public void deserialize(JsonElement element) {
    }

    @Override
    public JsonObject serialize() {
        return null;
    }
}