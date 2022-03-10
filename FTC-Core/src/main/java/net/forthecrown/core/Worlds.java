package net.forthecrown.core;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftNamespacedKey;

import java.util.Objects;

public interface Worlds {
    String
            OVERWORLD_NAME  = "world",
            END_NAME        = "world_the_end",
            NETHER_NAME     = "world_the_nether",
            VOID_NAME       = "world_void",
            RESOURCE_NAME   = "world_resource";

    NamespacedKey
            OVERWORLD_KEY   = ofLevelKey(Level.OVERWORLD),
            END_KEY         = ofLevelKey(Level.END),
            NETHER_KEY      = ofLevelKey(Level.NETHER),
            VOID_KEY        = Keys.minecraft(VOID_NAME),
            RW_KEY          = Keys.minecraft(RESOURCE_NAME);

    World   OVERWORLD       = nonNull(OVERWORLD_KEY);

    private static World nonNull(NamespacedKey key) {
        return Objects.requireNonNull(Bukkit.getWorld(key));
    }

    private static NamespacedKey ofLevelKey(ResourceKey<Level> key) {
        return CraftNamespacedKey.fromMinecraft(key.location());
    }

    static World voidWorld() {
        return nonNull(VOID_KEY);
    }

    static World end() {
        return nonNull(END_KEY);
    }

    static World resource() {
        return nonNull(RW_KEY);
    }

    static World nether() {
        return nonNull(NETHER_KEY);
    }
}
