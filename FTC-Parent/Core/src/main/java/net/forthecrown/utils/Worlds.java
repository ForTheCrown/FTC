package net.forthecrown.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Objects;

public interface Worlds {
    World NORMAL =      Objects.requireNonNull(Bukkit.getWorld("world"));
    World VOID =        Objects.requireNonNull(Bukkit.getWorld("world_void"));
    World END =         Objects.requireNonNull(Bukkit.getWorld("world_the_end"));
    World RESOURCE =    Objects.requireNonNull(Bukkit.getWorld("world_resource"));
    World SENATE =      Objects.requireNonNull(Bukkit.getWorld("world_senate"));
}
