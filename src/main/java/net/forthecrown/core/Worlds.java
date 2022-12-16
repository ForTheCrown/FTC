package net.forthecrown.core;

import net.forthecrown.core.registry.Keys;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftNamespacedKey;

import java.util.Objects;

/**
 * Class for world constants
 */
public final class Worlds {
    private Worlds() {}

    // World names
    public static final String
            VOID_NAME       = "world_void",
            RESOURCE_NAME   = "world_resource";

    // World keys
    public static final NamespacedKey
            OVERWORLD_KEY   = ofLevelKey(Level.OVERWORLD),
            END_KEY         = ofLevelKey(Level.END),
            NETHER_KEY      = ofLevelKey(Level.NETHER),
            VOID_KEY        = Keys.minecraft(VOID_NAME),
            RW_KEY          = Keys.minecraft(RESOURCE_NAME);

    public static World nonNull(NamespacedKey key) {
        return Objects.requireNonNull(Bukkit.getWorld(key));
    }

    public static NamespacedKey ofLevelKey(ResourceKey<Level> key) {
        return CraftNamespacedKey.fromMinecraft(key.location());
    }

    public static World overworld() {
        return nonNull(OVERWORLD_KEY);
    }

    /**
     * Gets the void world
     * @return The Void World
     */
    public static World voidWorld() {
        return nonNull(VOID_KEY);
    }

    /**
     * Gets the end
     * @return The End World
     */
    public static World end() {
        return nonNull(END_KEY);
    }

    /**
     * Gets the resource world
     * @return The Resource World
     */
    public static World resource() {
        return nonNull(RW_KEY);
    }

    /**
     * Gets the nether world
     * @return The Nether world
     */
    public static World nether() {
        return nonNull(NETHER_KEY);
    }
}