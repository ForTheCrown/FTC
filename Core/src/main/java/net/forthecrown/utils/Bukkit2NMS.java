package net.forthecrown.utils;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.entity.Player;

public interface Bukkit2NMS {
    static Entity getEntity(org.bukkit.entity.Entity entity) {
        return ((CraftEntity) entity).getHandle();
    }

    static ServerPlayer getPlayer(Player player) {
        return (ServerPlayer) getEntity(player);
    }

    static Level getLevel(World world) {
        return ((CraftWorld) world).getHandle();
    }

    static BlockState getState(Block block) {
        return ((CraftBlock) block).getNMS();
    }

    static BlockEntity getBlockEntity(TileState state) {
        return ((CraftBlockEntityState) state).getTileEntity();
    }
}