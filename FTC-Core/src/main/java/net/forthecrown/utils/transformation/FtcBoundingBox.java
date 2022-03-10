package net.forthecrown.utils.transformation;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.math.ImmutableVector3i;
import net.forthecrown.utils.math.Vector3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.Validate;
import org.bukkit.*;
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
import java.util.function.Predicate;

public class FtcBoundingBox implements IFtcBoundingBox<FtcBoundingBox> {

    private final World world;
    private int
            minX, minY, minZ,
            maxX, maxY, maxZ;

    public FtcBoundingBox(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.world = Validate.notNull(world, "World is null");
        resize(x1, y1, z1, x2, y2, z2);
    }

    public FtcBoundingBox(World world, double x, double y, double z, double x1, double y1, double z1) {
        this(world, (int) x, (int) y, (int) z, (int) x1, (int) y1, (int) z1);
    }

    public static FtcBoundingBox of(Location location, double radius){
        return of(location.clone().subtract(radius, radius, radius), location.clone().add(radius, radius, radius));
    }

    public static FtcBoundingBox of(Location loc1, Location loc2){
        Validate.isTrue(loc1.getWorld().equals(loc2.getWorld()), "Given locations had different worlds");

        return new FtcBoundingBox(loc1.getWorld(), loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ(), loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ());
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
        return of(border.getCenter(), border.getSize() / 2);
    }

    public static FtcBoundingBox of(Block b1, Block b2) {
        return of(b1.getLocation(), b2.getLocation());
    }

    public static FtcBoundingBox of(World world, ImmutableVector3i center, double radius) {
        int x = center.getX();
        int y = center.getY();
        int z = center.getZ();

        return new FtcBoundingBox(world, x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
    }

    public static FtcBoundingBox of(World world, ImmutableVector3i min, ImmutableVector3i max) {
        return new FtcBoundingBox(
                world,
                min.getX(), min.getY(), min.getZ(),
                max.getX(), max.getY(), max.getZ()
        );
    }

    public static FtcBoundingBox of(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        Vector3i min = Vector3i.of(json.get("min"));
        Vector3i max = Vector3i.of(json.get("max"));

        return of(Bukkit.getWorld(json.getString("world")), min, max);
    }

    public static FtcBoundingBox of(Tag t) {
        CompoundTag tag = (CompoundTag) t;
        World world = Bukkit.getWorld(tag.getString("world"));
        int[] cords = tag.getIntArray("cords");

        return new FtcBoundingBox(world, cords[0], cords[1], cords[2], cords[3], cords[4], cords[5]);
    }

    public FtcBoundingBox resize(int x1, int y1, int z1, int x2, int y2, int z2) {
        minX = Math.min(x1, x2);
        minY = Math.min(y1, y2);
        minZ = Math.min(z1, z2);
        maxX = Math.max(x1, x2);
        maxY = Math.max(y1, y2);
        maxZ = Math.max(z1, z2);
        return this;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
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

    public Collection<Player> getPlayers(){
        return getEntitiesByType(Player.class);
    }

    public Collection<Entity> getEntities(){
        return getWorld().getNearbyEntities(toBukkit());
    }

    public <T extends Entity> Collection<T> getEntitiesByType(Class<? extends T> type){
        return getWorld().getNearbyEntitiesByType(type, getCenterLocation(), sizeX()/2, sizeY()/2, sizeZ()/2);
    }

    public Collection<LivingEntity> getLivingEntities(){
        return getEntitiesByType(LivingEntity.class);
    }

    public Collection<Entity> getEntities(@Nullable Predicate<Entity> predicate) {
        return getWorld().getNearbyEntities(toBukkit(), predicate);
    }

    public boolean contains(int x, int y, int z) {
        return x >= minX && x < maxX && y >= minY && y < maxY && z >= minZ && z < maxZ;
    }

    public boolean contains(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return this.minX <= minX && this.maxX >= maxX && this.minY <= minY && this.maxY >= maxY && this.minZ <= minZ && this.maxZ >= maxZ;
    }

    public boolean overlaps(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return this.minX < maxX && this.maxX > minX && this.minY < maxY && this.maxY > minY && this.minZ < maxZ && this.maxZ > minZ;
    }

    @Override
    public FtcBoundingBox getThis() {
        return this;
    }

    @Override
    public FtcBoundingBox shrink(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return resize(
                getMinX() + minX,
                getMinY() + minY,
                getMinZ() + minZ,
                getMaxX() - maxX,
                getMaxY() - maxY,
                getMaxZ() - maxZ
        );
    }

    @Override
    public FtcBoundingBox expand(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return resize(
                getMinX() - minX,
                getMinY() - minY,
                getMinZ() - minZ,
                getMaxX() + maxX,
                getMaxY() + maxY,
                getMaxZ() + maxZ
        );
    }

    public BoundingBox toBukkit() {
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FtcBoundingBox blocks = (FtcBoundingBox) o;

        if (getMinX() != blocks.getMinX()) return false;
        if (getMinY() != blocks.getMinY()) return false;
        if (getMinZ() != blocks.getMinZ()) return false;
        if (getMaxX() != blocks.getMaxX()) return false;
        if (getMaxY() != blocks.getMaxY()) return false;
        if (getMaxZ() != blocks.getMaxZ()) return false;
        return getWorld().equals(blocks.getWorld());
    }

    @Override
    public int hashCode() {
        int result = getWorld().hashCode();
        result = 31 * result + getMinX();
        result = 31 * result + getMinY();
        result = 31 * result + getMinZ();
        result = 31 * result + getMaxX();
        result = 31 * result + getMaxY();
        result = 31 * result + getMaxZ();
        return result;
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
                world.getName() + ' ' +
                minX + ' ' +
                minY + ' ' +
                minZ + ' ' +
                " | " +
                maxX + ' ' +
                maxY + ' ' +
                maxZ + '}';
    }

    @Override
    public @NotNull FtcBoundingBox clone() {
        return new FtcBoundingBox(world, minX, minY, minZ, maxX, maxY, maxZ);
    }

    @NotNull
    @Override
    public BlockIterator iterator() {
        return new BlockIterator(this);
    }

    public void copyTo(Vector3i pos) { copyTo(world, pos); }

    public void copyTo(World world, Vector3i pos) {
        BoundingBoxes.copyTo(world, toVanilla(), pos.toWorldVector(world));
    }

    public net.minecraft.world.level.levelgen.structure.BoundingBox toVanilla() {
        Vector3i min = getMin();
        Vector3i max = getMax();

        return new net.minecraft.world.level.levelgen.structure.BoundingBox(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.empty();

        json.add("world", getWorld().getName());
        json.add("min", getMin());
        json.add("max", getMax());

        return json.getSource();
    }

    public Tag save() {
        int[] cords = new int[] { minX, minY, minZ, maxX, maxY, maxZ };
        IntArrayTag cordTag = new IntArrayTag(cords);

        CompoundTag result = new CompoundTag();
        result.put("cords", cordTag);
        result.putString("world", getWorld().getName());

        return result;
    }
}
