package net.forthecrown.core;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

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

    @NotNull
    public World getWorld() {
        return world;
    }

    public Location getCenterLocation(){
        return getCenterLocation(0, 0);
    }

    public Location getMinLocattion(){
        return getMinLocation(0, 0);
    }

    public Location getMaxLocation(){
        return getMaxLocation(0, 0);
    }

    public Location getCenterLocation(float yaw, float pitch){
        return getCenter().toLocation(getWorld(), yaw, pitch);
    }

    public Location getMinLocation(float yaw, float pitch){
        return getMin().toLocation(getWorld(), yaw, pitch);
    }

    public Location getMaxLocation(float yaw, float pitch){
        return getMax().toLocation(getWorld(), yaw, pitch);
    }

    public Collection<Player> getPlayers(){
        return getEntitiesByType(Player.class);
    }

    public Collection<Entity> getEntities(){
        return getWorld().getNearbyEntities(this);
    }

    public <T extends Entity> Collection<T> getEntitiesByType(Class<? extends T> type){
        return getWorld().getNearbyEntitiesByType(type, getCenterLocation(), getWidthX()/2, getHeight()/2, getWidthZ()/2);
    }

    public Collection<LivingEntity> getLivingEntities(){
        return getEntitiesByType(LivingEntity.class);
    }

    public boolean contains(CrownBoundingBox box){
        if(!getWorld().equals(box.getWorld())) return false;
        return super.contains(box);
    }

    public boolean overlaps(CrownBoundingBox box){
        if(!getWorld().equals(box.getWorld())) return false;
        return super.overlaps(box);
    }

    public boolean contains(Location location){
        if(!getWorld().equals(location.getWorld())) return false;
        return super.contains(location.toVector());
    }

    public boolean contains(Block block){
        return contains(block.getLocation());
    }

    public boolean contains(Entity entity){
        return contains(entity.getLocation());
    }

    public static CrownBoundingBox wrapBoundingBox(BoundingBox box, World world){
        return new CrownBoundingBox(world, box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMaxY(), box.getMaxZ());
    }
}
