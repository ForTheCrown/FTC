package net.forthecrown.emperor.user;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.admin.MuteStatus;
import net.forthecrown.emperor.user.data.TeleportRequest;
import net.forthecrown.emperor.utils.ListUtils;

import java.util.*;

public class FtcUserInteractions implements UserInteractions {

    public final FtcUser user;
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
        return incoming.get(0);
    }

    @Override
    public TeleportRequest firstOutgoing(){
        if(outgoing.size() < 1) return null;
        return outgoing.get(0);
    }
}
