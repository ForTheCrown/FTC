package net.forthecrown.vikings.valhalla.active;

import net.forthecrown.vikings.Vikings;
import net.forthecrown.vikings.valhalla.triggers.TriggerableEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TriggerContainer {

    public final TriggerListener listener;
    public ActiveRaid raid;

    public final int cellSizeX;
    public final int cellSizeZ;

    public Map<RaidCell, TriggerableEvent<PlayerMoveEvent>[]> moveEvents;
    public Map<RaidCell, TriggerableEvent<PlayerInteractEvent>[]> blockInteractEvents;
    public Map<RaidCell, TriggerableEvent<PlayerInteractEntityEvent>[]> entityInteractEvents;
    public Map<RaidCell, TriggerableEvent<EntityDeathEvent>[]> entityDieEvents;
    public Map<RaidCell, TriggerableEvent<EntityDamageEvent>[]> entityEvents;

    public TriggerContainer(int cellSizeX,
                            int cellSizeZ,
                            Map<RaidCell, TriggerableEvent<PlayerMoveEvent>[]> moveEvents,
                            Map<RaidCell, TriggerableEvent<PlayerInteractEvent>[]> blockInteractEvents,
                            Map<RaidCell, TriggerableEvent<PlayerInteractEntityEvent>[]> entityInteractEvents,
                            Map<RaidCell, TriggerableEvent<EntityDeathEvent>[]> entityDieEvents,
                            Map<RaidCell, TriggerableEvent<EntityDamageEvent>[]> entityEvents
    ){
        this.listener = new TriggerListener(this);

        this.cellSizeX = cellSizeX;
        this.cellSizeZ = cellSizeZ;

        this.moveEvents = moveEvents;
        this.blockInteractEvents = blockInteractEvents;
        this.entityInteractEvents = entityInteractEvents;
        this.entityDieEvents = entityDieEvents;
        this.entityEvents = entityEvents;
    }

    public RaidCell cellAt(Location location){
        Location minLoc = raid.getRaid().region.getMinLocation();
        Location localized = location.clone().subtract(minLoc);

        int cellX = localized.getBlockX() / cellSizeX;
        int cellZ = localized.getBlockZ() / cellSizeZ;

        return new RaidCell(cellX, cellZ);
    }

    public <E extends Event> void attemptEventExecution(RaidCell cell, Map<RaidCell, TriggerableEvent<E>[]> map, E event, Player player){
        if(!map.containsKey(cell)) Vikings.logger.info("Map doesn't contain value: " + cell.toString());
        TriggerableEvent<E>[] events = map.get(cell);
        List<Integer> toRemove = new ArrayList<>();

        for (int i = 0; i < events.length; i++){
            TriggerableEvent<E> e = events[i];

            if(e.removeAfterFirstExec() && e.testAndRun(player, raid, event)) toRemove.add(i);
        }

        map.put(cell, filterOutRemoved(events, toRemove));
    }

    private <E extends Event> TriggerableEvent<E>[] filterOutRemoved(TriggerableEvent<E>[] from, List<Integer> toFilter){
        if(toFilter.size() < 1) return from;

        List<TriggerableEvent<E>> events = new ArrayList<>(Arrays.asList(from));//Fuck this retarded shit, rEmOvE iS uNsUpPorTeD
        for (int i: toFilter) events.remove(i);

        return events.toArray(TriggerableEvent[]::new);
    }
}
