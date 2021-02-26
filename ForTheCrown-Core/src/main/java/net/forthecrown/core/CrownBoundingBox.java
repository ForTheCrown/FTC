package net.forthecrown.core;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.Collection;
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

    public World getWorld() {
        return world;
    }

    public List<Player> getPlayersIn(){
        List<Player> temp = new ArrayList<>();
        for (Entity e: world.getNearbyEntities(this)){
            if(!(e instanceof Player)) continue;
            temp.add((Player) e);
        }
        return temp;
    }

    public Collection<Entity> getResidingEntities(){
        Collection<Entity> temp = new ArrayList<>();
        for (Entity e: world.getNearbyEntities(this)){
            temp.add(e);
        }
        return temp;
    }

    public Collection<LivingEntity> getResidingLivingEntities(){
        Collection<LivingEntity> temp = new ArrayList<>();
        for (Entity e: world.getNearbyEntities(this)){
            if(!(e instanceof LivingEntity)) continue;
            temp.add((LivingEntity) e);
        }
        return temp;
    }

    public boolean contains(CrownBoundingBox box){
        if(!getWorld().equals(box.getWorld())) return false;
        return contains(box);
    }

    public boolean contains(Location location){
        if(!getWorld().equals(location.getWorld())) return false;
        return contains(location.getX(), location.getY(), location.getZ());
    }

    public static CrownBoundingBox wrapBoundingBox(BoundingBox box, World world){
        return new CrownBoundingBox(world, box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMaxY(), box.getMaxZ());
    }
}
