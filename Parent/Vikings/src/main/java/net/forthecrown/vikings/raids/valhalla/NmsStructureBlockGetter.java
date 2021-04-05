package net.forthecrown.vikings.raids.valhalla;

import net.minecraft.server.v1_16_R3.TileEntityStructure;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftStructureBlock;

public class NmsStructureBlockGetter extends CraftStructureBlock {
    public NmsStructureBlockGetter(Block block) {
        super(block);
    }

    @Override
    public TileEntityStructure getSnapshot() {
        return super.getSnapshot();
    }
}
