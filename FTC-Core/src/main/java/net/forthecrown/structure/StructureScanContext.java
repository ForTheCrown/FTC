package net.forthecrown.structure;

import net.forthecrown.utils.math.Vector3i;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.function.Predicate;

public class StructureScanContext {
    private boolean includeEntities;
    private final World world;
    private Vector3i start, size;
    private Predicate<Block> blockFilter;
    private Predicate<Entity> entityFilter;

    public StructureScanContext(World world, Vector3i start, Vector3i size) {
        this.world = world;
        this.start = start;
        this.size = size;
    }

    public StructureScanContext includeEntities(boolean scanEntities) {
        this.includeEntities = scanEntities;
        return this;
    }

    public StructureScanContext start(Vector3i startPos) {
        this.start = startPos;
        return this;
    }

    public StructureScanContext size(Vector3i size) {
        this.size = size;
        return this;
    }

    public StructureScanContext blockFilter(Predicate<Block> blockFilter) {
        this.blockFilter = blockFilter;
        return this;
    }

    public StructureScanContext entityFilter(Predicate<Entity> entityFilter) {
        this.entityFilter = entityFilter;
        return this;
    }

    public boolean includeEntities() {
        return includeEntities;
    }

    public World world() {
        return world;
    }

    public Vector3i start() {
        return start;
    }

    public Vector3i size() {
        return size;
    }

    public Predicate<Block> blockFilter() {
        return blockFilter;
    }

    public Predicate<Entity> entityFilter() {
        return entityFilter;
    }

    boolean filterBlock(Block block) {
        if(blockFilter == null) return true;
        return blockFilter.test(block);
    }

    boolean filterEntity(Entity entity) {
        if(entityFilter == null) return true;
        return entityFilter.test(entity);
    }
}
