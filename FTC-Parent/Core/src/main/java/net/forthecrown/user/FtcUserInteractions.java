package net.forthecrown.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.serializer.JsonDeserializable;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.user.data.TeleportRequest;
import net.forthecrown.utils.JsonUtils;

import java.util.*;

public class FtcUserInteractions implements UserInteractions, JsonSerializable, JsonDeserializable {

    public final FtcUser user;

    public UUID lastMarriageRequest;
    public UUID waitingFinish;
    public UUID marriedTo;
    public long lastMarriageChange;

    public boolean acceptingProposals;
    public boolean marriageChatToggled;

    public Set<UUID> blocked = new HashSet<>();
    public Set<UUID> separated = new HashSet<>();

    private final List<TeleportRequest> incoming = new ArrayList<>();
    private final List<TeleportRequest> outgoing = new ArrayList<>();

    public FtcUserInteractions(FtcUser user){
        this.user = user;
    }

    @Override
    public MuteStatus muteStatus() {
        return ForTheCrown.getPunishmentManager().checkMuteSilent(user.getUniqueId());
    }

    @Override
    public Set<UUID> getBlockedUsers() {
        return blocked;
    }

    @Override
    public CrownUser getUser() {
        return user;
    }

    @Override
    public void addBlocked(UUID id) {
        blocked.add(id);
    }

    @Override
    public void removeBlocked(UUID id) {
        blocked.remove(id);
    }

    @Override
    public boolean isOnlyBlocked(UUID uuid) {
        return blocked.contains(uuid);
    }

    @Override
    public boolean serversAllowed() {
        return true;
    }

    @Override
    public boolean realmsAllowed() {
        return true;
    }

    @Override
    public boolean chatAllowed() {
        return muteStatus().maySpeak;
    }

    @Override
    public boolean telemetryAllowed() {
        return false;
    }

    @Override
    public boolean isBlockedPlayer(UUID uuid) {
        return blocked.contains(uuid) || separated.contains(uuid);
    }

    @Override
    public boolean isSeparatedPlayer(UUID id){
        return separated.contains(id);
    }

    @Override
    public void addSeparated(UUID uuid) {
        separated.add(uuid);
    }

    @Override
    public void removeSeparated(UUID id) {
        separated.remove(id);
    }

    @Override
    public void handleTeleport(TeleportRequest request){
        this.outgoing.add(request);
        request.startCountdown();
        request.getReceiver().getInteractions().receiveTeleport(request);
    }

    @Override
    public void receiveTeleport(TeleportRequest request){
        this.incoming.add(request);
    }

    @Override
    public void clearIncoming(){ doClearStuff(incoming); }

    @Override
    public void clearOutgoing(){ doClearStuff(outgoing); }

    private void doClearStuff(List<TeleportRequest> requestMap){
        new ArrayList<>(requestMap).forEach(TeleportRequest::stop);
        requestMap.clear();
    }

    @Override
    public TeleportRequest getIncoming(CrownUser user){
        return getFromUser(incoming, user, true);
    }

    @Override
    public TeleportRequest getOutgoing(CrownUser user){
        return getFromUser(outgoing, user, false);
    }

    public TeleportRequest getFromUser(List<TeleportRequest> requests, CrownUser user, boolean checkSender){
        for (TeleportRequest r: requests){
            if(checkSender) if(r.getSender().equals(user)) return r;
            else if(r.getReceiver().equals(user)) return r;
        }

        return null;
    }

    @Override
    public List<TeleportRequest> getCurrentOutgoing(){
        return outgoing;
    }

    @Override
    public List<TeleportRequest> getCurrentIncoming(){
        return incoming;
    }

    @Override
    public void removeIncoming(CrownUser from){
        TeleportRequest r = getFromUser(incoming, from, true);
        if(r == null) return;

        incoming.remove(r);
    }

    @Override
    public void removeOutgoing(CrownUser to){
        TeleportRequest r = getFromUser(outgoing, to, false);
        if(r == null) return;

        outgoing.remove(r);
    }

    @Override
    public TeleportRequest firstIncoming(){
        if(incoming.size() < 1) return null;
        return incoming.get(incoming.size()-1);
    }

    @Override
    public TeleportRequest firstOutgoing(){
        if(outgoing.size() < 1) return null;
        return outgoing.get(incoming.size()-1);
    }

    @Override
    public UUID getMarriedTo() {
        return marriedTo;
    }

    @Override
    public void setMarriedTo(UUID marriedTo) {
        this.marriedTo = marriedTo;
    }

    @Override
    public long getLastMarriageChange() {
        return lastMarriageChange;
    }

    @Override
    public void setLastMarriageChange(long lastMarriageChange) {
        this.lastMarriageChange = lastMarriageChange;
    }

    @Override
    public boolean canChangeMarriageStatus(){
        if(lastMarriageChange == 0) return true;

        long nextAllowed = lastMarriageChange + ForTheCrown.getMarriageCooldown();
        return System.currentTimeMillis() > nextAllowed;
    }

    @Override
    public UUID getLastProposal() {
        return lastMarriageRequest;
    }

    @Override
    public void setLastProposal(UUID lastMarriageRequest) {
        this.lastMarriageRequest = lastMarriageRequest;
    }

    @Override
    public boolean acceptingProposals() {
        return acceptingProposals;
    }

    @Override
    public void setAcceptingProposals(boolean acceptingProposals) {
        this.acceptingProposals = acceptingProposals;
    }

    @Override
    public UUID getWaitingFinish() {
        return waitingFinish;
    }

    @Override
    public void setWaitingFinish(UUID waitingFinish) {
        this.waitingFinish = waitingFinish;
    }

    @Override
    public boolean mChatToggled() {
        return marriageChatToggled;
    }

    @Override
    public void setMChatToggled(boolean marriageChatToggled) {
        this.marriageChatToggled = marriageChatToggled;
    }

    @Override
    public JsonObject serialize() {
        JsonBuf json = JsonBuf.empty();

        if(marriageChatToggled) json.add("marriageChat", true);
        if(!acceptingProposals) json.add("acceptingProposals", false);
        if(marriedTo != null) json.addUUID("marriedTo", marriedTo);
        if(lastMarriageChange != 0) json.add("lastMarriage", lastMarriageChange);

        if(!blocked.isEmpty()) json.addList("blocked", blocked, JsonUtils::writeUUID);
        if(!separated.isEmpty()) json.addList("separated", separated, JsonUtils::writeUUID);

        return json.nullIfEmpty();
    }

    @Override
    public void deserialize(JsonElement element) {
        separated.clear();
        blocked.clear();

        marriageChatToggled = false;
        acceptingProposals = true;
        lastMarriageChange = 0L;
        marriedTo = null;
        lastMarriageRequest = null;
        waitingFinish = null;

        if(element == null) return;
        JsonBuf json = JsonBuf.of(element.getAsJsonObject());

        marriageChatToggled = json.getBool("marriageChat");
        acceptingProposals = json.getBool("acceptingProposals");

        marriedTo = json.getUUID("marriedTo");
        lastMarriageChange = json.getLong("lastMarriage");

        Collection<UUID> blockedIDs = json.getList("blocked", e -> UUID.fromString(e.getAsString()));
        if(blockedIDs != null) blocked.addAll(blockedIDs);

        Collection<UUID> separatedIDs = json.getList("separated", e -> UUID.fromString(e.getAsString()));
        if(separatedIDs != null) separated.addAll(separatedIDs);
    }
}
