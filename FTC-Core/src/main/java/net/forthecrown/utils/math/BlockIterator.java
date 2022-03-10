package net.forthecrown.utils.math;

import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.NoSuchElementException;

public class BlockIterator extends AbstractPosIterator<Vector3i, Block> {
    private final World world;

    public BlockIterator(World world, Vector3i min, Vector3i max, long maxIteration) {
        super(min, max, maxIteration);
        this.world = world;
    }

    @Override
    public Block next() {
        if(!hasNext()) throw new NoSuchElementException();
        advance();

        return world.getBlockAt(x, y, z);
    }
}
