package net.forthecrown.vikings.valhalla.active;

import com.google.common.collect.ImmutableList;
import net.forthecrown.vikings.valhalla.VikingRaid;
import net.forthecrown.vikings.valhalla.builder.BattleBuilder;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import java.util.List;

public class RaidParty {

    public final VikingRaid raid;
    public ActiveRaid activeRaid;

    private List<Player> participants;

    public RaidParty(VikingRaid raid) {
        this.raid = raid;
    }

    public void start(){
        BattleBuilder builder = new BattleBuilder(this);
        activeRaid = builder.build();


    }

    public boolean hasStarted(){
        return activeRaid != null;
    }

    public boolean contains(Player player){
        return participants.contains(player);
    }

    public void join(Player player){
        Validate.isTrue(!hasStarted(), "Raid has already started");

        participants.add(player);
    }

    public void remove(Player player){
        participants.remove(player);
    }

    public List<Player> getParticipants() {
        return ImmutableList.copyOf(participants);
    }
}
