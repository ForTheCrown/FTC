package net.forthecrown.vikings.raids;

import net.forthecrown.vikings.Vikings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

//Class existence reason, provide VikingRaid with a way of handling multiple players
//And dynamically changing the difficulty and handling the players
public class RaidParty implements Iterable<Player>{

    public final RaidDifficulty difficulty;
    public final VikingRaid selectedRaid;
    public final int startsInMinutes;
    public final long startTime;

    private final Set<Player> participants;
    private float difficultyModifier;
    private boolean specialsAllowed;

    public RaidParty(RaidDifficulty difficulty, VikingRaid selectedRaid, @Nonnegative int startsInMinutes, Player... players){
        this.difficulty = difficulty;
        this.selectedRaid = selectedRaid;
        this.startsInMinutes = startsInMinutes;
        this.startTime = System.currentTimeMillis() + startsInMinutes*60*1000;

        this.participants = new HashSet<>(Arrays.asList(players));
        if(startsInMinutes > 0) createCountdown();
    }

    private void createCountdown(){
        Bukkit.getScheduler().scheduleSyncDelayedTask(Vikings.getInstance(), () -> {
            forEach(player -> player.sendMessage("Raid starting!"));
            startRaid();
        }, startsInMinutes*60*20);
    }

    public void startRaid(){
        setSpecialsAllowed(participants.size() > 3);
        difficultyModifier = difficulty.modifier + ((float) participants.size())/2;
        selectedRaid.initRaid(this);
    }

    public void leaveParty(Player player){
        participants.remove(player);
        player.teleport(RaidManager.EXIT_LOCATION);

        if(selectedRaid.isInUse() && participants.size() < 1){
            selectedRaid.onLose();
        }
    }

    public void joinParty(Player player){
        if(selectedRaid.isInUse()) return;

        participants.add(player);
    }

    public float getModifier() {
        return difficultyModifier;
    }

    public Set<Player> getParticipants() {
        return participants;
    }

    public boolean specialsAllowed() {
        return specialsAllowed;
    }

    public void setSpecialsAllowed(boolean specialsAllowed) {
        this.specialsAllowed = specialsAllowed;
    }

    public void teleport(Location location){
        for (Player p: participants){
            p.teleport(location);
        }
    }

    @Override
    public void forEach(Consumer<? super Player> action){
        for (Player p: getParticipants()){
            action.accept(p);
        }
    }

    @NotNull
    @Override
    public Iterator<Player> iterator() {
        return getParticipants().iterator();
    }
}
