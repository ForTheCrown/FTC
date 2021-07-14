package net.forthecrown.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Objects;

/**
 * World constants
 */
public interface Worlds {
    World OVERWORLD =   Objects.requireNonNull(Bukkit.getWorld("world"));
    World VOID =        Objects.requireNonNull(Bukkit.getWorld("world_void"));
    World END =         Objects.requireNonNull(Bukkit.getWorld("world_the_end"));
    World RESOURCE =    Objects.requireNonNull(Bukkit.getWorld("world_resource"));
    World SENATE =      Objects.requireNonNull(Bukkit.getWorld("world_senate"));
    World RAIDS =       Objects.requireNonNull(Bukkit.getWorld("world_raids"));
}
