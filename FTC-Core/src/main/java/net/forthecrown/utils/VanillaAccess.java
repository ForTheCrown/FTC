package net.forthecrown.utils;

import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.entity.Player;

/**
 * Utility class for adapting bukkit objects to vanilla MC
 */
public interface VanillaAccess {
    static Entity getEntity(org.bukkit.entity.Entity entity) {
        return ((CraftEntity) entity).getHandle();
    }

    static ServerPlayer getPlayer(Player player) {
        return (ServerPlayer) getEntity(player);
    }

    static ServerLevel getLevel(World world) {
        return ((CraftWorld) world).getHandle();
    }

    static BlockState getState(Block block) {
        return ((CraftBlock) block).getNMS();
    }

    static BlockEntity getBlockEntity(TileState state) {
        return ((CraftBlockEntityState) state).getTileEntity();
    }

    static DedicatedServer getServer() {
        return ((CraftServer) Bukkit.getServer()).getServer();
    }
}