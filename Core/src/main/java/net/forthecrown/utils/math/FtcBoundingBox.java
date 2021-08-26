package net.forthecrown.utils.math;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

/**
 * FtcAxisAlignedBoundingBox
 * Like a regular BoundingBox, but it's tied to a world, so it has more operational abilities
 * And it can be iterated through
 */
public class FtcBoundingBox extends BoundingBox implements Iterable<Block> {

    private final World world;

    public FtcBoundingBox(World world, double x1, double y1, double z1, double x2, double y2, double z2) {
        super(x1, y1, z1, x2, y2, z2);
        this.world = Validate.notNull(world, "World is null");
    }

    public static FtcBoundingBox of(Block block){
        return new FtcBoundingBox(block.getWorld(), block.getX(), block.getY(), block.getZ(), block.getX()+1, block.getY()+1, block.getZ()+1);
    }

    public static FtcBoundingBox of(Location location, double radius){
        return of(location.clone().subtract(radius, radius, radius), location.clone().add(radius, radius, radius));
    }

    public static FtcBoundingBox of(Location loc1, Location loc2){
        Validate.isTrue(loc1.getWorld().equals(loc2.getWorld()), "Given locations had different worlds");

        return new FtcBoundingBox(loc1.getWorld(), loc1.getX(), loc1.getY(), loc1.getZ(), loc2.getX(), loc2.getY(), loc2.getZ());
    }

    public static FtcBoundingBox of(Location location, double xRadius, double height, double zRadius){
        return of(location.clone().subtract(xRadius, height, zRadius), location.clone().add(xRadius, height, zRadius));
    }

    public static FtcBoundingBox of(World world, BoundingBox box){
        return new FtcBoundingBox(world, box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMaxY(), box.getMaxZ());
    }

    public static FtcBoundingBox of(World world, net.minecraft.world.level.levelgen.structure.BoundingBox nms) {
        return new FtcBoundingBox(world, nms.minX(), nms.minY(), nms.minZ(), nms.maxX(), nms.maxY(), nms.maxZ());
    }

    public static FtcBoundingBox of(World world){
        WorldBorder border = world.getWorldBorder();
        return of(border.getCenter(), border.getSize());
    }

    public static FtcBoundingBox of(Block b1, Block b2) {
        return of(b1.getLocation(), b2.getLocation());
    }

    public static FtcBoundingBox deserialize(Map<String, Object> args) {
        double minX = 0.0D;
        double minY = 0.0D;
        double minZ = 0.0D;
        double maxX = 0.0D;
        double maxY = 0.0D;
        double maxZ = 0.0D;
        World world = Bukkit.getWorld(String.valueOf(args.get("world")));

        if (args.containsKey("minX")) {
            minX = ((Number)args.get("minX")).doubleValue();
        }

        if (args.containsKey("minY")) {
            minY = ((Number)args.get("minY")).doubleValue();
        }

        if (args.containsKey("minZ")) {
            minZ = ((Number)args.get("minZ")).doubleValue();
        }

        if (args.containsKey("maxX")) {
            maxX = ((Number)args.get("maxX")).doubleValue();
        }

        if (args.containsKey("maxY")) {
            maxY = ((Number)args.get("maxY")).doubleValue();
        }

        if (args.containsKey("maxZ")) {
            maxZ = ((Number)args.get("maxZ")).doubleValue();
        }

        return new FtcBoundingBox(world, minX, minY, minZ, maxX, maxY, maxZ);
    }

    @NotNull
    public World getWorld() {
        return world;
    }

    public FtcBoundingBox shrink(double amount) {
        double minX = getMinX() - amount;
        double minY = getMinY() - amount;
        double minZ = getMinZ() - amount;

        double maxX = getMaxX() - amount;
        double maxY = getMaxY() - amount;
        double maxZ = getMaxZ() - amount;

        return new FtcBoundingBox(world, minX, minY, minZ, maxX, maxY, maxZ);
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

    public boolean contains(FtcBoundingBox box){
        if(!getWorld().equals(box.getWorld())) return false;
        return super.contains(box);
    }

    public boolean overlaps(FtcBoundingBox box){
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
        FtcBoundingBox that = (FtcBoundingBox) o;
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
                "world=" + world.getName() +
                ",minX=" + getMinX() +
                ",minY=" + getMinY() +
                ",minZ=" + getMinZ() +
                ",maxX=" + getMaxX() +
                ",maxY=" + getMaxY() +
                ",maxZ=" + getMaxZ() +
                '}';
    }

    @Override
    public @NotNull FtcBoundingBox clone() {
        return of(getWorld(), this);
    }

    @NotNull
    @Override
    public BoundingBoxIterator iterator() {
        return new BoundingBoxIterator(this);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("world", world.getName());

        return result;
    }

    public void copyTo(Vector3i pos) { copyTo(world, pos); }

    public void copyTo(World world, Vector3i pos) {
        BoundingBoxes.copyTo(world, toVanilla(), pos.toWorldVector(world));
    }

    public net.minecraft.world.level.levelgen.structure.BoundingBox toVanilla() {
        Vector min = getMin();
        Vector max = getMax();

        return new net.minecraft.world.level.levelgen.structure.BoundingBox(min.getBlockX(), min.getBlockY(), min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ());
    }
}
