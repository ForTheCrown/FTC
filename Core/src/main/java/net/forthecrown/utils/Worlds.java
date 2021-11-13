package net.forthecrown.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Objects;

/**
 * World constants
 */
public interface Worlds {
    World
            OVERWORLD       = nonNullWorld("world"),
            VOID            = nonNullWorld("world_void"),
            END             = nonNullWorld("world_the_end"),
            RESOURCE        = nonNullWorld("world_resource"),
            SENATE          = nonNullWorld("world_senate");

    private static World nonNullWorld(String name) {
        return Objects.requireNonNull(Bukkit.getWorld(name));
    }
}
