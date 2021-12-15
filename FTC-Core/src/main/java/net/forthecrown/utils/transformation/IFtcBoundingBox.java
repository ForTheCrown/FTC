package net.forthecrown.utils.transformation;

import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.math.Vector3i;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public interface IFtcBoundingBox<T extends IFtcBoundingBox<T>> extends
        JsonSerializable,
        Iterable<Block>, Cloneable
{
    int getMinX();
    int getMinY();
    int getMinZ();

    int getMaxX();
    int getMaxY();
    int getMaxZ();

    World getWorld();

    default Vector3i getMin() {
        return new Vector3i(getMinX(), getMinY(), getMinZ());
    }

    default Vector3i getMax() {
        return new Vector3i(getMaxX(), getMaxY(), getMaxZ());
    }

    default int getCenterX() {
        return centerCord(getMinX(), getMaxX());
    }

    default int getCenterY() {
        return centerCord(getMinY(), getMaxY());
    }

    default int getCenterZ() {
        return centerCord(getMinZ(), getMaxZ());
    }

    static int centerCord(int minCord, int maxCord) {
        return minCord + ((maxCord - minCord) / 2);
    }

    default Vector3i getCenter() {
        return new Vector3i(getCenterX(), getCenterY(), getCenterZ());
    }

    default int sizeX() {
        return getMaxX() - getMinX();
    }

    default int sizeZ() {
        return getMaxZ() - getMinZ();
    }

    default int sizeY() {
        return getMaxY() - getMinY();
    }

    default int getVolume() {
        return sizeY() * sizeX() * sizeZ();
    }

    default Location getCenterLocation(){
        return getCenterLocation(0, 0);
    }

    default Location getMinLocation(){
        return getMinLocation(0, 0);
    }

    default Location getMaxLocation(){
        return getMaxLocation(0, 0);
    }

    default Location getCenterLocation(float yaw, float pitch){
        return getCenter().toLoc(getWorld(), yaw, pitch);
    }

    default Location getMinLocation(float yaw, float pitch){
        return getMin().toLoc(getWorld(), yaw, pitch);
    }

    default Location getMaxLocation(float yaw, float pitch){
        return getMax().toLoc(getWorld(), yaw, pitch);
    }

    boolean contains(int x, int y, int z);
    boolean contains(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);
    boolean overlaps(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);

    default boolean contains(T box){
        if(!getWorld().equals(box.getWorld())) return false;
        return contains(box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMaxY(), box.getMaxZ());
    }

    default boolean overlaps(T box){
        if(!getWorld().equals(box.getWorld())) return false;
        return overlaps(box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMaxY(), box.getMaxZ());
    }

    default boolean contains(Location location){
        if(!getWorld().equals(location.getWorld())) return false;
        return contains(location.toVector());
    }

    default boolean contains(Vector vector) {
        return contains(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    default boolean contains(Block block) {
        return contains(block.getX(), block.getY(), block.getZ());
    }

    default boolean contains(Entity entity) {
        return contains(entity.getLocation());
    }
    
    T getThis();

    T shrink(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);
    T expand(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);
    T resize(int x1, int y1, int z1, int x2, int y2, int z2);
    
    default T expand(BlockFace direction, int amount) {
        Vector v = direction.getDirection();
        return expand(v.getBlockX(), v.getBlockY(), v.getBlockZ(), amount);
    }
    
    default T expand(Vector3i direction, int amount) {
        return expand(direction.getX(), direction.getY(), direction.getZ(), amount);
    }
    
    default T expand(int xDir, int yDir, int zDir, int amount) {
        if((xDir == 0 && yDir == 0 && zDir == 0) || amount == 0) return getThis();

        int negativeX = xDir < 0 ? -xDir * amount : 0;
        int negativeY = yDir < 0 ? -yDir * amount : 0;
        int negativeZ = zDir < 0 ? -zDir * amount : 0;
        int positiveX = xDir > 0 ? xDir * amount : 0;
        int positiveY = yDir > 0 ? yDir * amount : 0;
        int positiveZ = zDir > 0 ? zDir * amount : 0;
        return expand(negativeX, negativeY, negativeZ, positiveX, positiveY, positiveZ);
    }

    default T expand(int amount) {
        return expand(amount, amount, amount, amount, amount, amount);
    }
}
