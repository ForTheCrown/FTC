package net.forthecrown.user.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.Messages;
import net.forthecrown.user.*;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class UserInteractions extends UserComponent {

    //Transient variables declared just for convenience
    //So you'd know what is and isn't serialized in this class

    /**
     * The last user this person sent a marriage
     * proposal to
     */
    @Getter @Setter
    private transient UUID lastProposal;

    /**
     * The ID of the player this user is waiting to
     * finish their marriage with
     */
    @Getter @Setter
    private transient UUID waitingFinish;

    /**
     * This user's spouse's ID
     */
    @Getter @Setter
    private UUID spouse;

    /**
     * All players blocked by this user
     */
    @Getter
    private final Set<UUID> blocked = new ObjectOpenHashSet<>();

    /**
     * All players this user this forcefully seperated from
     */
    @Getter
    private final Set<UUID> separated = new ObjectOpenHashSet<>();

    /**
     * All incoming teleport requests
     */
    @Getter
    private transient final List<TeleportRequest> incoming = new ArrayList<>();

    /**
     * All outgoing teleport requests
     */
    @Getter
    private transient final List<TeleportRequest> outgoing = new ArrayList<>();

    public UserInteractions(User user, ComponentType<UserInteractions> type) {
        super(user, type);
    }

    /**
     * Divorces the user this interactions object is attached to.
     * @throws IllegalArgumentException If the user is not married
     */
    public void divorce() throws IllegalArgumentException {
        Validate.isTrue(isMarried(), "User is not married, cannot divorce");

        var spouse = spouseUser();

        setSpouse(null);
        spouse.getInteractions().setSpouse(null);

        // Ensure neither is in marriage chat
        user.set(Properties.MARRIAGE_CHAT, false);
        spouse.set(Properties.MARRIAGE_CHAT, false);

        user.sendMessage(Messages.senderDivorced(spouse));
        spouse.sendOrMail(Messages.targetDivorced(user));

        spouse.unloadIfOffline();
    }

    /**
     * Adds the given UUID to the blocked users list
     * @param id the ID to add
     */
    public void addBlocked(UUID id) {
        blocked.add(id);
    }

    /**
     * Removes the given UUID from the blocked users list
     * @param id The ID to remove
     */
    public void removeBlocked(UUID id) {
        blocked.remove(id);
    }

    /**
     * Checks if the given ID is only blocked and not force separated from this user
     * @param uuid The ID to check for
     * @return If the given ID is only blocked by the player
     */
    public boolean isOnlyBlocked(UUID uuid) {
        return blocked.contains(uuid);
    }

    /**
     * Tests if the given UUID is either blocked or seperated from
     * this player
     * @param uuid The UUID to test
     * @return True, if the given ID is either blocked or seperated from this player
     */
    public boolean isBlockedPlayer(UUID uuid) {
        return blocked.contains(uuid) || separated.contains(uuid);
    }

    /**
     * Checks if the given ID is force separated from this player
     * @param id The ID to check
     * @return Whether the given ID is separated from this user
     */
    public boolean isSeparatedPlayer(UUID id){
        return separated.contains(id);
    }

    /**
     * Adds an ID to separated players list
     * @param uuid the ID to add
     */
    public void addSeparated(UUID uuid) {
        separated.add(uuid);
    }

    /**
     * Removes an ID to separated players list
     * @param id the ID to remove
     */
    public void removeSeparated(UUID id) {
        separated.remove(id);
    }

    /**
     * Adds an outgoing request
     * @param request The request to add
     */
    public void addOutgoing(TeleportRequest request) {
        this.outgoing.add(0, request);
    }

    /**
     * Adds an incoming teleport request
     * @param request the request to add
     */
    public void addIncoming(TeleportRequest request) {
        this.incoming.add(0, request);
    }

    /**
     * Clears all incoming TP requests
     */
    public void clearIncoming() {
        clearRequestList(incoming);
    }

    /**
     * Clears all outgoing TP requests
     */
    public void clearOutgoing() {
        clearRequestList(outgoing);
    }

    /**
     * Properly ensures the map of teleport requests is cancelled
     * by copying the list and then stopping each request.
     * <p>
     * This is done to avoid a {@link java.util.ConcurrentModificationException}
     * while calling {@link TeleportRequest#stop()} on each
     * list element
     * @param requests The requests to cancel
     */
    private void clearRequestList(List<TeleportRequest> requests) {
        List.copyOf(requests).forEach(TeleportRequest::stop);
        requests.clear();
    }

    /**
     * Gets an incoming request from the given user
     * @param user The user to get the request of
     * @return The request sent by the user, or null, if the user hasn't sent a request
     */
    public TeleportRequest getIncoming(User user){
        return findRequest(incoming, user, true);
    }

    /**
     * Gets an outgoing request to the given user
     * @param user The user to get the request of
     * @return The request sent to the user, or null, if the user hasn't sent a request
     */
    public TeleportRequest getOutgoing(User user){
        return findRequest(outgoing, user, false);
    }

    /**
     * Finds a request by a given user in the given teleport request list
     * @param requests The list to search
     * @param user The user to search fro
     * @param checkSender True, if this search should test the given user
     *                    against the requests' sender or target.
     * @return The found request, null, if none was found
     */
    private static TeleportRequest findRequest(List<TeleportRequest> requests, User user, boolean checkSender) {
        for (TeleportRequest r: requests) {
            if (checkSender) {
                if(r.getSender().equals(user)) {
                    return r;
                }

            } else if (r.getTarget().equals(user)) {
                return r;
            }
        }

        return null;
    }

    /**
     * Removes an incoming TP request from the given sender
     * @param from The user that sent the request
     * @return True, if an element was removed from the list, false otherwise.
     */
    public boolean removeIncoming(User from){
        return removeRequest(incoming, true, from);
    }

    /**
     * Removes an outgoing TP request from the given receiver
     * @param to The user that received the request
     * @return True, if an element was removed from the list, false otherwise.
     */
    public boolean removeOutgoing(User to){
        return removeRequest(outgoing, false, to);
    }

    /**
     * Removes a request from the given request list
     * @param requests The request list to remove from
     * @param checkSender True, if it should check the request's sender
     *                    to find a valid request to remove
     * @param user The user to remove the request of
     * @return True, if an element was removed from the list, false otherwise.
     */
    private static boolean removeRequest(List<TeleportRequest> requests, boolean checkSender, User user) {
        var request = findRequest(requests, user, checkSender);

        if (request == null) {
            return false;
        }

        return requests.remove(request);
    }

    /**
     * Gets the first incoming request to this user
     * @return The first incoming request to this user, null, if the user hasn't received any requests
     */
    public TeleportRequest latestIncoming(){
        return latestRequest(incoming);
    }

    /**
     * Gets the first outgoing request from this user
     * @return The first outgoing request from this user, null, if the user hasn't sent any requests
     */
    public TeleportRequest latestOutgoing() {
        return latestRequest(outgoing);
    }

    /**
     * Gets the most recent teleport request added to the list
     * <p>
     * Most recent is applied loosly here, as all this really
     * does is return the first entry in the list if it's
     * non-null, this is because requests are inserted into the
     * list at index 0
     *
     * @param requests The list of teleports to find the most recent one from
     * @return The most recent request, or null, if the list is empty
     */
    private static TeleportRequest latestRequest(List<TeleportRequest> requests) {
        return requests.isEmpty() ? null : requests.get(0);
    }

    /**
     * Gets the user object of this user's spouse
     * @return The spouse's User, or null, if not married
     */
    public User spouseUser(){
        if (getSpouse() == null) {
            return null;
        }

        return Users.get(getSpouse());
    }

    /**
     * Checks if the user is married
     * @return true if married, false if not
     */
    public boolean isMarried() {
        return getSpouse() != null;
    }

    @Override
    public JsonObject serialize() {
        JsonWrapper json = JsonWrapper.create();

        if(spouse != null) {
            json.addUUID("spouse", spouse);
        }

        if(!blocked.isEmpty()) {
            json.addList("blocked", blocked, JsonUtils::writeUUID);
        }

        if (!separated.isEmpty()) {
            json.addList("separated", separated, JsonUtils::writeUUID);
        }

        return json.nullIfEmpty();
    }

    @Override
    public void deserialize(JsonElement element) {
        separated.clear();
        blocked.clear();

        spouse = null;
        lastProposal = null;
        waitingFinish = null;

        if(element == null) {
            return;
        }

        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

        if (json.has("spouse")) {
            this.spouse = json.getUUID("spouse");
        } else {
            this.spouse = json.getUUID("marriedTo");
        }

        blocked.addAll(json.getList("blocked", JsonUtils::readUUID));
        separated.addAll(json.getList("separated", JsonUtils::readUUID));
    }

}