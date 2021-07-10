package net.forthecrown.utils.math;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Like a regular BoundingBox, but it's tied to a world, so it has more operational abilities
 * And it can be iterated through
 */
public class CrownRegion extends BoundingBox implements Iterable<Block> {

    private final World world;

    public CrownRegion(World world, double x1, double y1, double z1, double x2, double y2, double z2) {
        super(x1, y1, z1, x2, y2, z2);
        this.world = world;
    }

    public CrownRegion(Location loc1, Location loc2){
        super(loc1.getX(), loc1.getY(), loc1.getZ(), loc2.getX(), loc2.getY(), loc2.getZ());

        if(!loc1.getWorld().equals(loc2.getWorld())) throw new IllegalArgumentException("Location 1 and Location 2 cannot have different worlds");
        this.world = loc1.getWorld();
    }

    public static CrownRegion of(Block block){
        return new CrownRegion(block.getWorld(), block.getX(), block.getY(), block.getZ(), block.getX()+1, block.getY()+1, block.getZ()+1);
    }

    public static CrownRegion of(Location location, double radius){
        return new CrownRegion(location.clone().subtract(radius, radius, radius), location.clone().add(radius, radius, radius));
    }

    public static CrownRegion of(Location location, Location location1){
        return new CrownRegion(location1, location);
    }

    public static CrownRegion of(Location location, double xRadius, double height, double zRadius){
        return new CrownRegion(location.clone().subtract(xRadius, height, zRadius), location.clone().add(xRadius, height, zRadius));
    }

    public static CrownRegion of(BoundingBox box, World world){
        return new CrownRegion(world, box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMaxY(), box.getMaxZ());
    }

    public static CrownRegion of(World world){
        WorldBorder border = world.getWorldBorder();
        return of(border.getCenter(), border.getSize());
    }

    @NotNull
    public World getWorld() {
        return world;
    }

    public Location getCenterLocation(){
        return getCenterLocation(0, 0);
    }

    public Location getMinLocation(){
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

    public boolean contains(CrownRegion box){
        if(!getWorld().equals(box.getWorld())) return false;
        return super.contains(box);
    }

    public boolean overlaps(CrownRegion box){
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CrownRegion that = (CrownRegion) o;
        return getWorld().equals(that.getWorld());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getWorld());
    }

    public List<Block> getBlocks(){
        return getBlocks(null);
    }

    public List<Block> getBlocks(@Nullable Predicate<Block> predicate){
        List<Block> blocks = new ArrayList<>();

        //How in the absolute hell does this work xD
        for (Block b : this) {
            if (predicate == null || predicate.test(b)) blocks.add(b);
        }

        return blocks;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' +
                "world=" + world +
                ",minX=" + getMinX() +
                ",minY=" + getMinY() +
                ",minZ=" + getMinZ() +
                ",maxX=" + getMaxX() +
                ",maxY=" + getMaxY() +
                ",maxZ=" + getMaxZ() +
                '}';
    }

    @Override
    public @NotNull CrownRegion clone() {
        return of(this, getWorld());
    }

    @NotNull
    @Override
    public BoundingBoxIterator iterator() {
        return new BoundingBoxIterator(this);
    }
}
