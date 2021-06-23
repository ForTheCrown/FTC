package net.forthecrown.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sk89q.worldedit.math.BlockVector3;
import net.forthecrown.core.serializer.JsonSerializable;
import net.minecraft.server.v1_16_R3.BlockPosition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class BlockPos implements JsonSerializable, Cloneable {
    public int x;
    public int y;
    public int z;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPos(int x, int z) {
        this.x = x;
        this.y = 0;
        this.z = z;
    }

    public BlockPos() { }

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

    public BlockPos setX(int x) {
        this.x = x;
        return this;
    }

    public int getY() {
        return y;
    }

    public BlockPos setY(int y) {
        this.y = y;
        return this;
    }

    public int getZ() {
        return z;
    }

    public BlockPos setZ(int z) {
        this.z = z;
        return this;
    }

    public BlockPos above(){ return new BlockPos(x, y+1, z); }
    public BlockPos below(){ return new BlockPos(x, y-1, z); }

    public BlockPos inDirection(BlockFace face){ return inDirection(face, 1); }

    public BlockPos inDirection(BlockFace face, int amount){
        final int x = face.getModX() * amount;
        final int y = face.getModY() * amount;
        final int z = face.getModZ() * amount;

        return new BlockPos(x + this.x, y + this.y, z + this.z);
    }

    public Block getBlock(World world){
        return world.getBlockAt(x, y, z);
    }

    public Material getMaterial(World world){
        return getBlock(world).getType();
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
    public BlockPos clone(){
        return new BlockPos(x, y, z);
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
