package net.forthecrown.structure;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.utils.math.WorldBounds3i;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.Objects;
import java.util.function.Predicate;

import static net.forthecrown.structure.BlockStructure.DEFAULT_PALETTE_NAME;

@Getter
public class StructureFillConfig {
    private final WorldBounds3i area;
    private final boolean includingFunctionBlocks;
    private final Predicate<Block> blockPredicate;
    private final Predicate<Entity> entityPredicate;

    private final String paletteName;

    private StructureFillConfig(Builder builder) {
        this.area = Objects.requireNonNull(builder.area);
        this.blockPredicate = builder.blockPredicate;
        this.entityPredicate = builder.entityPredicate;
        this.includingFunctionBlocks = builder.includeFunctionBlocks;

        this.paletteName = builder.paletteName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean includeBlock(Block block) {
        if (blockPredicate == null) {
            return true;
        }

        return blockPredicate.test(block);
    }

    public boolean includeEntity(Entity entity) {
        if (entityPredicate == null) {
            return true;
        }

        return entityPredicate.test(entity);
    }

    @Accessors(fluent = true, chain = true)
    @Setter @Getter
    public static class Builder {
        private WorldBounds3i area;
        private boolean includeFunctionBlocks;
        private Predicate<Block> blockPredicate = block -> true;
        private Predicate<Entity> entityPredicate = entity -> true;

        private String paletteName = DEFAULT_PALETTE_NAME;

        public StructureFillConfig build() {
            return new StructureFillConfig(this);
        }
    }
}