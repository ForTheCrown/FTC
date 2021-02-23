package net.forthecrown.marchevent;

import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.api.Announcer;
import net.forthecrown.marchevent.events.InEventListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

import java.util.*;

public class PvPEvent {

    public static Set<Player> inEvent = new HashSet<>();

    public static final List<ItemStack> itemList = Arrays.asList(
            new ItemStack(Material.STONE_SWORD, 1),
            new ItemStack(Material.ARROW, 6),
            new ItemStack(Material.BOW)
    );

    private final InEventListener inEventListener = new InEventListener();

    public static final BoundingBox yellowEntry = new BoundingBox(353, 127, 67, 355, 125, 65);
    public static final BoundingBox blueEntry = new BoundingBox(366, 125, 65, 368, 127, 67);

    public static final World eventWorld = Bukkit.getWorld("world_void");
    public static final Location yellowStart = new Location(eventWorld, 334, 124, 92);
    public static final Location blueStart = new Location(eventWorld, 388, 124, 92);
    public static final Location exitLocation = new Location(eventWorld, 361, 127, 65);
    public static final Location centerLocation = new Location(eventWorld, 360, 116, 91);

    public void startEvent(){
        EventMain.getInstance().getServer().getPluginManager().registerEvents(inEventListener, EventMain.getInstance());
    }

    public void endEvent(){
        HandlerList.unregisterAll(inEventListener);

        for (Player p: inEvent) {
            removePlayer(p);
        }
    }

    public void endEvent(String winner){
        endEvent();

        Announcer.acLiteral(CrownUtils.translateHexCodes(winner + " won the round!"));
    }

    public void resetEvent(){

    }

    public void removePlayer(Player p){
        p.teleport(exitLocation);
        inEvent.remove(p);
    }

    public void doCenterBlockCheck(){
        byte yellowBlocks = 0;
        byte blueBlocks = 0;
        final byte[] xMod = {0, 1, 0, -1};
        final byte[] yMod = {0, 0, 1, 0};
        Location toCheck = centerLocation.clone();

        for(int i = 0; i < 4; i++){
            Material mat = toCheck.add(xMod[i], 0, yMod[i]).getBlock().getType();

            if(mat == Material.CYAN_WOOL) blueBlocks++;
            else if(mat == Material.YELLOW_WOOL) yellowBlocks++;
        }

        if(yellowBlocks == 4) endEvent("&eYellow");
        if(blueBlocks == 4) endEvent("&cBlue");
    }

    public void moveToStartingPositions(){
        movePlayersInBboxToPlace(yellowEntry, yellowStart);
        movePlayersInBboxToPlace(blueEntry, blueStart);
    }

    public void movePlayersInBboxToPlace(BoundingBox boundingBox, Location loc){
        for (Entity e: eventWorld.getNearbyEntities(boundingBox)){
            if(!(e instanceof Player)) continue;
            e.teleport(loc);
        }
    }

    public boolean checkAllPlayersForItems(){
        for (Entity e: eventWorld.getNearbyEntities(yellowEntry)){
            if(e.getType() != EntityType.PLAYER) continue;

            Player p = (Player) e;
            if(!p.getInventory().isEmpty()){
                Announcer.acLiteral(p.getName() + " does not have a full inventory!");
                return false;
            }
        }

        for (Entity e: eventWorld.getNearbyEntities(blueEntry)){
            if(e.getType() != EntityType.PLAYER) continue;

            Player p = (Player) e;
            if(!p.getInventory().isEmpty()){
                Announcer.acLiteral(p.getName() + " does not have a full inventory!");
                return false;
            }
        }

        return true;
    }
}
