package net.forthecrown.vikings.valhalla.active;

import net.forthecrown.vikings.valhalla.triggers.TriggerableEvent;
import org.bukkit.Location;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;

public class TriggerContainer {

    public final TriggerListener listener;
    public ActiveRaid raid;

    public final int cellSizeX;
    public final int cellSizeZ;

    public Map<RaidCell, TriggerableEvent<PlayerMoveEvent>[]> moveEvents;
    public Map<RaidCell, TriggerableEvent<PlayerInteractEvent>[]> blockInteractEvents;
    public Map<RaidCell, TriggerableEvent<PlayerInteractEntityEvent>[]> entityInteractEvents;
    public TriggerableEvent<EntityDeathEvent>[] entityDieEvents;
    public TriggerableEvent<EntityDamageEvent>[] entityEvents;

    public TriggerContainer(int cellSizeX,
                            int cellSizeZ,
                            Map<RaidCell, TriggerableEvent<PlayerMoveEvent>[]> moveEvents,
                            Map<RaidCell, TriggerableEvent<PlayerInteractEvent>[]> blockInteractEvents,
                            Map<RaidCell, TriggerableEvent<PlayerInteractEntityEvent>[]> entityInteractEvents,
                            TriggerableEvent<EntityDeathEvent>[] entityDieEvents,
                            TriggerableEvent<EntityDamageEvent>[] entityEvents
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
        Location minLoc = raid.raid.region.getMin().toLocation(location.getWorld());
        Location localized = location.clone().subtract(minLoc);

        int cellX = localized.getBlockX() / cellSizeX;
        int cellZ = localized.getBlockZ() / cellSizeZ;

        return new RaidCell(cellX, cellZ);
    }
}
