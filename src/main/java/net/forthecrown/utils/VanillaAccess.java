package net.forthecrown.utils;

import net.minecraft.core.MappedRegistry;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_19_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

/**
 * Utility class for accessing vanilla code
 */
public final class VanillaAccess {
    private VanillaAccess() {}

    /**
     * Gets a vanilla entity object
     * @param entity The bukkit enity
     * @return The vanilla entity
     */
    public static Entity getEntity(org.bukkit.entity.Entity entity) {
        return ((CraftEntity) entity).getHandle();
    }

    /**
     * Gets a vanilla player object
     * @param player The bukkit player
     * @return The vanilla player
     */
    public static ServerPlayer getPlayer(Player player) {
        return (ServerPlayer) getEntity(player);
    }

    /**
     * Gets a vanilla level
     * @param world The bukkit level
     * @return The vanilla level
     */
    public static ServerLevel getLevel(World world) {
        return ((CraftWorld) world).getHandle();
    }

    /**
     * Gets a block state from a bukkit block
     * @param block The bukkit block
     * @return The block state
     */
    public static BlockState getState(Block block) {
        return ((CraftBlock) block).getNMS();
    }

    public static BlockState getState(BlockData data) {
        return ((CraftBlockData) data).getState();
    }

    /**
     * Gets the vanilla tile entity instance
     * @param state The bukkit tile entity
     * @return The vanilla equivalent
     */
    public static BlockEntity getBlockEntity(TileState state) {
        return ((CraftBlockEntityState) state).getTileEntity();
    }

    /**
     * Gets the vanilla server instance
     * @return The vanilla server instance
     */
    public static DedicatedServer getServer() {
        return ((CraftServer) Bukkit.getServer()).getServer();
    }

    /**
     * Gets the player's server-side packet listener
     * @param player The player to get the packet listener of
     * @return The player's packet listener
     */
    public static ServerGamePacketListenerImpl getPacketListener(Player player) {
        return getPlayer(player).connection;
    }

    public static Rotation toVanilla(net.forthecrown.structure.Rotation rotation) {
        return switch (rotation) {
            case COUNTERCLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
            case CLOCKWISE_180 -> Rotation.CLOCKWISE_180;
            case CLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    /**
     * Unfreezes the given registry
     * @param registry The registry to unfreeze
     */
    public static void unfreeze(MappedRegistry registry) {
        try {
            // The only liability here is the frozen variable
            // It may change with each release but I also don't
            // believe we could do some kind of
            // for each method (if method == boolean) lookup,
            // as they may just add a different boolean variable
            // to the class for whatever reason
            Field frozen = findFrozenField(registry.getClass());
            frozen.setAccessible(true);

            frozen.setBoolean(registry, false);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static final String FROZEN_FIELD = "ca";

    private static Field findFrozenField(Class c) throws NoSuchFieldException {
        for (var f: c.getDeclaredFields()) {
            if (f.getType() == Boolean.TYPE
                    || f.getType() == Boolean.class
            ) {
                return f;
            }
        }

        return c.getDeclaredField(FROZEN_FIELD);
    }
}