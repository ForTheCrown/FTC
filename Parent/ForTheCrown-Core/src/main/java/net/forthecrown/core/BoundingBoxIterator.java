package net.forthecrown.core;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Iterator;
import java.util.NoSuchElementException;

//I need you to understand that I hate myself so much right now
//All of this code is stolen... from https://github.com/EngineHub/WorldEdit/blob/8377a6bdacb4d5e0b0af2d97e097e3d946cf1a0d/worldedit-core/src/main/java/com/sk89q/worldedit/regions/iterator/RegionIterator.java
//World Edit's RegionIterator
public class BoundingBoxIterator implements Iterator<Block> {

    private final CrownBoundingBox region;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private final Location min;
    private int nextX;
    private int nextY;
    private int nextZ;

    public BoundingBoxIterator(CrownBoundingBox box){
        Validate.notNull(box);
        this.region = box;

        Location max = box.getMaxLocation();
        this.maxX = max.getBlockX();
        this.maxY = max.getBlockY();
        this.maxZ = max.getBlockZ();

        min = box.getMinLocation();
        this.nextX = min.getBlockX();
        this.nextY = min.getBlockY();
        this.nextZ = min.getBlockZ();
    }

    @Override
    public boolean hasNext() {
        return nextX != Integer.MIN_VALUE;
    }

    private void forward() {
        while (hasNext() && !region.contains(nextX, nextY, nextZ)) {
            forwardOne();
        }
    }

    private void forwardOne() {
        if (nextX++ <= maxX) return;
        nextX = min.getBlockX();

        if (nextY++ <= maxY) return;
        nextY = min.getBlockY();

        if (nextZ++ <= maxZ) return;
        nextX = Integer.MIN_VALUE;
    }

    @Override
    public Block next() {
        if (!hasNext()) throw new NoSuchElementException();
        Block answer = region.getWorld().getBlockAt(nextX, nextY, nextZ);

        forwardOne();
        forward();

        return answer;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
