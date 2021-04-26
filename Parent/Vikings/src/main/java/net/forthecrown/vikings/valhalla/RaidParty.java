package net.forthecrown.vikings.valhalla;

import net.forthecrown.core.utils.ListUtils;
import net.forthecrown.vikings.Vikings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

//Class existence reason, provide VikingRaid with a way of handling multiple players
//And dynamically changing the difficulty and handling the players
public class RaidParty implements Iterable<Player>{

    public final DynamicDifficulty difficulty;
    public final VikingRaid selectedRaid;
    public final int startsInTicks;
    public final long startTime;

    private final List<Player> participants;
    private Player host;
    public boolean specialsAllowed;

    public RaidParty(@NotNull VikingRaid selectedRaid, @Nonnegative int startsInTicks, @NotNull Collection<Player> players){
        this.difficulty = new DynamicDifficulty(this, StaticDifficulty.NORMAL);
        this.selectedRaid = selectedRaid;
        this.startsInTicks = startsInTicks;
        this.startTime = System.currentTimeMillis() + startsInTicks*50;

        this.participants = new ArrayList<>(players);
        if(startsInTicks > 0) createCountdown();
    }

    private void createCountdown(){
        Bukkit.getScheduler().scheduleSyncDelayedTask(Vikings.inst(), () -> {
            forEach(player -> player.sendMessage("Raid starting!"));
            startRaid();
        }, startsInTicks);
    }

    public void startRaid(){
        specialsAllowed = participants.size() > 2;
        difficulty.calculateModifier();
        selectedRaid.init(this);
    }

    public void leave(Player player){
        player.teleport(RaidManager.EXIT_LOCATION);
        participants.remove(player);

        if(selectedRaid.isActive() && participants.size() < 1){
            selectedRaid.end(VikingRaid.EndCause.LOSS);
            return;
        }
        if(host == null || host.equals(player)) replaceHost();
    }

    public void join(Player player){
        if(selectedRaid.isActive()) return;
        if(participants.size() < 1) setHost(player);

        participants.add(player);
    }

    public Collection<Player> getParticipants() {
        return participants;
    }

    public void teleport(Location location){
        for (Player p: participants){
            p.teleport(location);
        }
    }

    public Player getHost() {
        return host;
    }

    public void replaceHost(){
        if(ListUtils.isNullOrEmpty(participants)) return;
        setHost(participants.get(participants.size() == 1 ? 0 : RandomUtils.nextInt(participants.size())));
    }

    public void setHost(@NotNull Player host) {
        if(this.host != null) this.host.sendMessage(Component.text("You are no longer raid host").color(NamedTextColor.GRAY));
        this.host = host;
        host.sendMessage(Component.text("You are now raid host").color(NamedTextColor.YELLOW));
    }

    @Override
    public void forEach(Consumer<? super Player> action){
        participants.forEach(action);
    }

    @NotNull
    @Override
    public Iterator<Player> iterator() {
        return participants.iterator();
    }
}
