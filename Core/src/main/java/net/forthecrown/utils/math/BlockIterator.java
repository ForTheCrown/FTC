package net.forthecrown.utils.math;

import org.bukkit.block.Block;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class BlockIterator implements Iterator<Block> {
    private final FtcBoundingBox region;
    private int zIteration = 0;
    private final int requiredIterations;

    private int nextX, nextY, nextZ;

    private final int
            startX, startY, startZ,
            maxX, maxY, maxZ;

    public BlockIterator(FtcBoundingBox region) {
        this.region = region;

        startX = region.getMinX();
        startY = region.getMinY();
        startZ = region.getMinZ();

        maxX = region.getMaxX();
        maxY = region.getMaxY();
        maxZ = region.getMaxZ();

        requiredIterations = region.sizeZ();

        nextX = startX;
        nextY = startY;
        nextZ = startZ;
    }

    private void calculateNextCords() {
        nextX++;
        if(nextX > maxX) {
            nextX = startX;
            nextY++;

            if(nextY > maxY) {
                nextY = startY;
                nextZ++;
                zIteration++;

                if(nextZ > maxZ) nextZ = startZ;
            }
        }

    }

    @Override
    public boolean hasNext() {
        return zIteration < requiredIterations;
    }

    @Override
    public Block next() {
        if(!hasNext()) throw new NoSuchElementException();

        calculateNextCords();

        return region.getWorld().getBlockAt(nextX, nextY, nextZ);
    }
}
