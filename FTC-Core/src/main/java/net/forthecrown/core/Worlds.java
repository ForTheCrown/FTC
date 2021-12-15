package net.forthecrown.core;

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
            RESOURCE        = nullableWorld("world_resource");
                            // resource world throws exception, cuz null, if
                            // nonNullWorld is used yet isn't null when I use
                            // nullableWorld, what kind of fuckery is this

    private static World nonNullWorld(String name) {
        return Objects.requireNonNull(nullableWorld(name));
    }

    private static World nullableWorld(String name) {
        return Bukkit.getWorld(name);
    }
}
