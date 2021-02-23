package net.forthecrown.core;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public class CrownBoundingBox extends BoundingBox {

    private final World world;

    public CrownBoundingBox(World world, double x1, double y1, double z1, double x2, double y2, double z2) {
        super(x1, y1, z1, x2, y2, z2);
        this.world = world;
    }

    public CrownBoundingBox(Location loc1, Location loc2){
        super(loc1.getX(), loc1.getY(), loc1.getZ(), loc2.getX(), loc2.getY(), loc2.getZ());

        if(!loc1.getWorld().equals(loc2.getWorld())) throw new IllegalArgumentException("Location 1 and Location 2 cannot have different worlds");
        this.world = loc1.getWorld();
    }

    public List<Player> getPlayersIn(){
        List<Player> temp = new ArrayList<>();
        for (Entity e: world.getNearbyEntities(this)){
            if(!(e instanceof Player)) continue;
            temp.add((Player) e);
        }
        return temp;
    }
}
