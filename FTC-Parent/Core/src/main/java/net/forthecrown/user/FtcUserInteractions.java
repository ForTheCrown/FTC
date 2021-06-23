package net.forthecrown.user;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.user.data.TeleportRequest;
import net.forthecrown.utils.ListUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FtcUserInteractions implements UserInteractions {

    public final FtcUser user;

    public UUID lastMarriageRequest;
    public UUID waitingFinish;
    public UUID marriedTo;
    public long lastMarriageStatusChange;
    public boolean acceptingProposals;
    public boolean marriageChatToggled;

    public Set<UUID> blocked;

    private final List<TeleportRequest> incoming = new ArrayList<>();
    private final List<TeleportRequest> outgoing = new ArrayList<>();

    public FtcUserInteractions(FtcUser user){
        this.user = user;
    }

    public void reload(List<String> raw){
        this.blocked = ListUtils.convertToSet(raw, UUID::fromString);
    }

    public List<String> save(){
        return ListUtils.convertToList(blocked, UUID::toString);
    }

    public Map<String, Object> serializeMarriages(){
        Map<String, Object> result = new HashMap<>();

        if(marriageChatToggled) result.put("MarriageChat", true);
        if(!acceptingProposals) result.put("AcceptingProposals", false);
        if(marriedTo != null) result.put("MarriedTo", marriedTo.toString());
        if(lastMarriageStatusChange != 0) result.put("LastMarriageAction", lastMarriageStatusChange);

        return result.isEmpty() ? null : result;
    }

    public void loadMarriages(@Nullable ConfigurationSection section){
        if(section == null){
            marriageChatToggled = false;
            acceptingProposals = true;
            lastMarriageStatusChange = 0L;
            marriedTo = null;
            lastMarriageRequest = null;
            waitingFinish = null;
            return;
        }

        marriageChatToggled = section.getBoolean("MarriageChat", false);
        acceptingProposals = section.getBoolean("AcceptingProposals", true);

        String marriedToString = section.getString("MarriedTo");
        if(marriedToString == null) this.marriedTo = null;
        else this.marriedTo = UUID.fromString(marriedToString);

        lastMarriageStatusChange = section.getLong("LastMarriageAction", 0L);
    }

    @Override
    public MuteStatus muteStatus() {
        return CrownCore.getPunishmentManager().checkMuteSilent(user.getUniqueId());
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
    public boolean isBlockedPlayer(UUID uuid) {
        return blocked.contains(uuid);
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
    public long getLastMarriageStatusChange() {
        return lastMarriageStatusChange;
    }

    @Override
    public void setLastMarriageStatusChange(long lastMarriageStatusChange) {
        this.lastMarriageStatusChange = lastMarriageStatusChange;
    }

    @Override
    public boolean canChangeMarriageStatus(){
        return (lastMarriageStatusChange + CrownCore.getMarriageCooldown()) <= System.currentTimeMillis();
    }

    @Override
    public UUID getLastMarriageRequest() {
        return lastMarriageRequest;
    }

    @Override
    public void setLastMarriageRequest(UUID lastMarriageRequest) {
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
    public boolean marriageChatToggled() {
        return marriageChatToggled;
    }

    @Override
    public void setMarriageChatToggled(boolean marriageChatToggled) {
        this.marriageChatToggled = marriageChatToggled;
    }
}
