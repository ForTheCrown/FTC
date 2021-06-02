package net.forthecrown.repop;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class QuickPos {

    public final int x;
    public final int y;
    public final int z;

    public QuickPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public QuickPos below(){
        return new QuickPos(x, y-1, z);
    }

    public QuickPos above(){
        return new QuickPos(x, y+1, z);
    }

    public Material blockAt(World world){
        return blockAt(world, this);
    }

    public static Material blockAt(World world, QuickPos pos){
        return world.getBlockAt(pos.x, pos.y, pos.z).getType();
    }

    public static QuickPos of(Location location){
        return new QuickPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
