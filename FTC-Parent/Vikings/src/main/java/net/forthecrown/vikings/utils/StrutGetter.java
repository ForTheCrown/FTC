package net.forthecrown.vikings.utils;

import net.forthecrown.core.utils.BlockPos;
import net.minecraft.server.v1_16_R3.TileEntityStructure;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftStructureBlock;

public class StrutGetter extends CraftStructureBlock {

    public StrutGetter(Block block) {
        super(block);
    }

    @Override
    public TileEntityStructure getSnapshot() {
        return super.getSnapshot();
    }

    public static TileEntityStructure of(Block block){
        return new StrutGetter(block).getTileEntity();
    }

    public static TileEntityStructure of(BlockPos pos, World world){
        return of(pos.getBlock(world));
    }
}
