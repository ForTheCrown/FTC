package net.forthecrown.vikings.valhalla.generation;

import net.minecraft.server.v1_16_R3.TileEntityStructure;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftStructureBlock;

public class NmsStruct extends CraftStructureBlock {
    public NmsStruct(Block block) {
        super(block);
    }

    @Override
    public TileEntityStructure getSnapshot() {
        return super.getSnapshot();
    }

    public static TileEntityStructure of(Block block){
        return new NmsStruct(block).getSnapshot();
    }
}
