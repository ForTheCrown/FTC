package net.forthecrown.utils.math;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.serializer.JsonSerializable;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class BlockOffset implements JsonSerializable {

    private final int xOffset;
    private final int yOffset;
    private final int zOffset;

    public BlockOffset(int xOffset, int yOffset, int zOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }

    public static BlockOffset of(BlockPos min, BlockPos max){
        BlockPos dif = max.clone().subtract(min);
        return new BlockOffset(dif.x, dif.y, dif.z);
    }

    public static BlockOffset of(Location min, Location max){
        Location loc = max.clone().subtract(min);
        return new BlockOffset(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static BlockOffset of(Location min, BlockPos pos){
        BlockPos dif = pos.clone().subtract(min);
        return new BlockOffset(dif.x, dif.y, dif.z);
    }

    public static BlockOffset of(Location min, int x, int y, int z){
        return new BlockOffset(x - min.getBlockX(), y - min.getBlockY(), z - min.getBlockZ());
    }

    public static BlockOffset deserialize(JsonElement element){
        JsonObject json = element.getAsJsonObject();

        int x = json.get("xOffset").getAsInt();
        int y = json.get("yOffset").getAsInt();
        int z = json.get("zOffset").getAsInt();

        return new BlockOffset(x, y, z);
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

    public BlockPos apply(BlockPos min){
        return min.clone().add(xOffset, yOffset, zOffset);
    }

    public Location apply(Location min){
        return min.clone().add(xOffset, yOffset, zOffset);
    }

    public Vector apply(Vector vector){
        return new Vector(vector.getX() + xOffset, vector.getY() + yOffset, vector.getZ() + zOffset);
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();

        json.addProperty("xOffset", xOffset);
        json.addProperty("yOffset", yOffset);
        json.addProperty("zOffset", zOffset);

        return json;
    }
}
