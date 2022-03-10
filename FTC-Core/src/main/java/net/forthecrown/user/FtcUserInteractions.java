package net.forthecrown.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.actions.TeleportRequest;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.TimeUtil;

import java.util.*;

public class FtcUserInteractions extends AbstractUserAttachment implements UserInteractions {

    //Transient variables declared just for convenience
    //So you'd know what is and isn't serialized in this class
    public transient UUID lastMarriageRequest;
    public transient UUID waitingFinish;
    public UUID spouse;
    public long lastMarriageChange;

    public boolean acceptingProposals;
    public boolean marriageChatToggled;

    public final Set<UUID> blocked = new ObjectOpenHashSet<>();
    public final Set<UUID> separated = new ObjectOpenHashSet<>();

    public transient final Set<UUID> invites = new ObjectOpenHashSet<>();
    public transient final Set<UUID> invitedTo = new ObjectOpenHashSet<>();

    private transient final List<TeleportRequest> incoming = new ArrayList<>();
    private transient final List<TeleportRequest> outgoing = new ArrayList<>();

    public FtcUserInteractions(FtcUser user){
        super(user, "interactions");
    }

    @Override
    public MuteStatus muteStatusSilent() {
        return Crown.getPunishments().checkMuteSilent(user.getUniqueId());
    }

    @Override
    public MuteStatus muteStatus() {
        return Crown.getPunishments().checkMute(getUser());
    }

    @Override
    public Set<UUID> getBlockedUsers() {
        return blocked;
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
    public boolean chatAllowed() {
        return muteStatusSilent().maySpeak;
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
    public void addOutgoing(TeleportRequest request) {
        this.outgoing.add(request);
    }

    @Override
    public void addIncoming(TeleportRequest request) {
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
            else if(r.getTarget().equals(user)) return r;
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
    public UUID getSpouse() {
        return spouse;
    }

    @Override
    public void setSpouse(UUID spouse) {
        this.spouse = spouse;
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
        return TimeUtil.hasCooldownEnded(FtcVars.marriageCooldown.get(), lastMarriageChange);
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
    public boolean hasBeenInvited(UUID by) {
        return invitedTo.contains(by);
    }

    @Override
    public boolean hasInvited(UUID target) {
        return invites.contains(target);
    }

    @Override
    public void addInvite(UUID target) {
        invites.add(target);
    }

    @Override
    public void addInvitedTo(UUID sender) {
        invitedTo.add(sender);
    }

    @Override
    public void removeInvite(UUID to) {
        invites.remove(to);
    }

    @Override
    public void removeInvitedTo(UUID from) {
        invitedTo.remove(from);
    }

    public void clearInvites() {
        UUID owner = user.getUniqueId();

        for (UUID id: invitedTo) {
            CrownUser user = UserManager.getUser(id);
            user.getInteractions().removeInvite(owner);
        }

        invitedTo.clear();

        for (UUID id: invites) {
            CrownUser user = UserManager.getUser(id);
            user.getInteractions().removeInvitedTo(owner);
        }

        invitedTo.clear();
    }

    @Override
    public JsonObject serialize() {
        JsonWrapper json = JsonWrapper.empty();

        if(marriageChatToggled) json.add("marriageChat", true);
        if(!acceptingProposals) json.add("acceptingProposals", false);
        if(spouse != null) json.addUUID("marriedTo", spouse);
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
        spouse = null;
        lastMarriageRequest = null;
        waitingFinish = null;

        if(element == null) return;
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        marriageChatToggled = json.getBool("marriageChat");
        acceptingProposals = json.getBool("acceptingProposals");

        spouse = json.getUUID("marriedTo");
        lastMarriageChange = json.getLong("lastMarriage");

        Collection<UUID> blockedIDs = json.getList("blocked", JsonUtils::readUUID);
        if(blockedIDs != null) blocked.addAll(blockedIDs);

        Collection<UUID> separatedIDs = json.getList("separated", JsonUtils::readUUID);
        if(separatedIDs != null) separated.addAll(separatedIDs);
    }
}
