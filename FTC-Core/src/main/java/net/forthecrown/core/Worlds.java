package net.forthecrown.core;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftNamespacedKey;

import java.util.Objects;

/**
 * Class for world constants
 */
public interface Worlds {
    // World names
    String
            OVERWORLD_NAME  = "world",
            END_NAME        = "world_the_end",
            NETHER_NAME     = "world_the_nether",
            VOID_NAME       = "world_void",
            RESOURCE_NAME   = "world_resource";

    // World keys
    NamespacedKey
            OVERWORLD_KEY   = ofLevelKey(Level.OVERWORLD),
            END_KEY         = ofLevelKey(Level.END),
            NETHER_KEY      = ofLevelKey(Level.NETHER),
            VOID_KEY        = Keys.minecraft(VOID_NAME),
            RW_KEY          = Keys.minecraft(RESOURCE_NAME);

    // Overworld Constant
    World   OVERWORLD       = nonNull(OVERWORLD_KEY);

    private static World nonNull(NamespacedKey key) {
        return Objects.requireNonNull(Bukkit.getWorld(key));
    }

    private static NamespacedKey ofLevelKey(ResourceKey<Level> key) {
        return CraftNamespacedKey.fromMinecraft(key.location());
    }

    /**
     * Gets the void world
     * @return The Void World
     */
    static World voidWorld() {
        return nonNull(VOID_KEY);
    }

    /**
     * Gets the end
     * @return The End World
     */
    static World end() {
        return nonNull(END_KEY);
    }

    /**
     * Gets the resource world
     * @return The Resource World
     */
    static World resource() {
        return nonNull(RW_KEY);
    }

    /**
     * Gets the nether world
     * @return The Nether world
     */
    static World nether() {
        return nonNull(NETHER_KEY);
    }
}