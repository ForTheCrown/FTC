package net.forthecrown.vikings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sk89q.worldedit.math.BlockVector3;
import net.forthecrown.emperor.serialization.JsonSerializable;
import net.minecraft.server.v1_16_R3.BlockPosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class BlockPos implements JsonSerializable {
    private int x;
    private int y;
    private int z;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static BlockPos of(Block block){
        return new BlockPos(block.getX(), block.getY(), block.getZ());
    }

    public static BlockPos of(Location location){
        return new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static BlockPos of(Entity entity){
        return of(entity.getLocation());
    }

    public static BlockPos of(JsonElement element){
        JsonObject json = element.getAsJsonObject();

        final int x = json.get("x").getAsInt();
        final int y = json.get("y").getAsInt();
        final int z = json.get("z").getAsInt();

        return new BlockPos(x, y, z);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public BlockPos above(){
        return new BlockPos(x, y+1, z);
    }

    public BlockPos below(){
        return new BlockPos(x, y-1, z);
    }

    public BlockPos inDirection(BlockFace face, int amount){
        final int x = face.getModX() * amount;
        final int y = face.getModY() * amount;
        final int z = face.getModZ() * amount;

        return new BlockPos(x, y, z);
    }

    public Location toLoc(World world){
        return new Location(world, x, y, z);
    }

    public Vector toVec(){
        return new Vector(x, y, z);
    }

    public BlockPosition toNms(){
        return new BlockPosition(x, y, z);
    }

    public BlockVector3 toWE(){
        return BlockVector3.at(x, y, z);
    }

    @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();

        json.addProperty("x", x);
        json.addProperty("y", y);
        json.addProperty("z", z);

        return json;
    }
}
