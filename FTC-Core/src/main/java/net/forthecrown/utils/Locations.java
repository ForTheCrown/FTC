package net.forthecrown.utils;

import com.sk89q.worldedit.math.Vector3;
import net.minecraft.nbt.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public interface Locations {
    static Tag save(Location location) {
        CompoundTag tag = new CompoundTag();
        ListTag pos = new ListTag();
        pos.add(DoubleTag.valueOf(location.getX()));
        pos.add(DoubleTag.valueOf(location.getY()));
        pos.add(DoubleTag.valueOf(location.getZ()));

        ListTag rot = new ListTag();
        if(location.getYaw() != 0F) rot.add(FloatTag.valueOf(location.getYaw()));
        if(location.getPitch() != 0F) rot.add(FloatTag.valueOf(location.getPitch()));

        tag.put("pos", pos);
        if(!rot.isEmpty()) tag.put("rot", rot);
        if(location.getWorld() != null) tag.putString("world", location.getWorld().getName());

        return tag;
    }

    static Location load(Tag tagg) {
        CompoundTag tag = (CompoundTag) tagg;
        ListTag pos = tag.getList("pos", Tag.TAG_DOUBLE);
        ListTag rot = tag.getList("rot", Tag.TAG_FLOAT);

        double x = pos.getDouble(0);
        double y = pos.getDouble(1);
        double z = pos.getDouble(2);
        float yaw = rot.getFloat(0);
        float pitch = rot.getFloat(1);

        String worldName = tag.getString("world");
        World world = worldName.isBlank() ? null : Bukkit.getWorld(worldName);

        return new Location(world, x, y, z, yaw, pitch);
    }

    static Location of(World world, Vector3 pos) {
        return new Location(world, pos.getX(), pos.getY(), pos.getZ());
    }
}