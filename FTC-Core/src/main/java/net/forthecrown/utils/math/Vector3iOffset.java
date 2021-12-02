package net.forthecrown.utils.math;

import com.google.common.base.MoreObjects;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * A class which holds the offset from a given pos.
 * <p>
 * Allows the easy applying of this offset as well with the
 * {@link Vector3iOffset#apply(Location)} method.
 * </p>
 */
public class Vector3iOffset implements ImmutableVector3i {
    public static final Vector3iOffset ZERO = new Vector3iOffset(0, 0, 0);

    private final int xOffset;
    private final int yOffset;
    private final int zOffset;

    public Vector3iOffset(int xOffset, int yOffset, int zOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }

    public static Vector3iOffset of(AbstractVector3i min, AbstractVector3i max){
        AbstractVector3i dif = max.clone().subtract(min);
        return new Vector3iOffset(dif.x, dif.y, dif.z);
    }

    public static Vector3iOffset of(Location min, Location max){
        Location loc = max.clone().subtract(min);
        return new Vector3iOffset(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static Vector3iOffset of(Location min, Vector3i pos){
        Vector3i dif = pos.clone().subtract(min);
        return new Vector3iOffset(dif.x, dif.y, dif.z);
    }

    public static Vector3iOffset of(Location min, int x, int y, int z){
        return new Vector3iOffset(x - min.getBlockX(), y - min.getBlockY(), z - min.getBlockZ());
    }

    public static Vector3iOffset deserialize(JsonElement element){
        JsonObject json = element.getAsJsonObject();

        int x = json.get("xOffset").getAsInt();
        int y = json.get("yOffset").getAsInt();
        int z = json.get("zOffset").getAsInt();

        return new Vector3iOffset(x, y, z);
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public int getZOffset() {
        return zOffset;
    }

    public Vector3i apply(int x, int y, int z) {
        return new Vector3i(x + xOffset, y + yOffset, z + zOffset);
    }

    public Vector apply(double x, double y, double z) {
        return new Vector(x + xOffset, y + yOffset, z + zOffset);
    }

    public Vector3i apply(Vector3i min){
        return min.clone().add(xOffset, yOffset, zOffset);
    }

    public WorldVec3i apply(WorldVec3i min) {
        return min.clone().add(xOffset, yOffset, zOffset);
    }

    public Location apply(Location min){
        return min.clone().add(xOffset, yOffset, zOffset);
    }

    public Location apply(Location min, float yaw, float pitch){
        return new Location(
                min.getWorld(),
                xOffset + min.getX(),
                yOffset + min.getY(),
                zOffset + min.getZ(),
                yaw, pitch
        );
    }

    public Vector apply(Vector vector){
        return new Vector(vector.getX() + xOffset, vector.getY() + yOffset, vector.getZ() + zOffset);
    }

    @Override
    public int getX() {
        return xOffset;
    }

    @Override
    public int getY() {
        return yOffset;
    }

    @Override
    public int getZ() {
        return zOffset;
    }

    @Override
    public Vector3iOffset clone() {
        return new Vector3iOffset(xOffset, yOffset, zOffset);
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();

        json.addProperty("xOffset", xOffset);
        json.addProperty("yOffset", yOffset);
        json.addProperty("zOffset", zOffset);

        return json;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("xOffset", xOffset)
                .add("yOffset", yOffset)
                .add("zOffset", zOffset)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Vector3iOffset offset = (Vector3iOffset) o;

        return new EqualsBuilder()
                .append(xOffset, offset.xOffset)
                .append(yOffset, offset.yOffset)
                .append(zOffset, offset.zOffset)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(xOffset)
                .append(yOffset)
                .append(zOffset)
                .toHashCode();
    }
}
