package net.forthecrown.structure;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.utils.math.Transform;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.math.vector.Vector3i;

import static net.forthecrown.structure.BlockStructure.DEFAULT_PALETTE_NAME;

@Getter
public class StructurePlaceConfig {
    private final World world;
    private final Vector3i destination;

    private final ImmutableList<BlockProcessor> processors;
    private final ImmutableMap<String, FunctionProcessor> functions;

    private final boolean placeEntities;

    private final Transform transform;

    private final String paletteName;

    private StructurePlaceConfig(Builder builder) {
        this.world = builder.world;
        this.destination = builder.pos;
        this.processors = builder.processors.build();
        this.functions = builder.functions.build();
        this.placeEntities = builder.placeEntities;

        this.paletteName = builder.paletteName();

        this.transform = builder.transform
                .addOffset(destination);
    }

    public static Builder builder() {
        return new Builder();
    }

    public @Nullable BlockInfo run(@NotNull BlockInfo original, @NotNull Vector3i offset) {
        var result = original.copy();
        original = original.copy();

        for (var p: processors) {
            result = p.process(original, result == null ? null : result.copy(), this);
        }

        return result;
    }

    @Getter @Setter
    @Accessors(chain = true, fluent = true)
    public static class Builder {
        private World world;
        private Vector3i pos;

        private final ImmutableList.Builder<BlockProcessor> processors = ImmutableList.builder();
        private final ImmutableMap.Builder<String, FunctionProcessor> functions = ImmutableMap.builder();

        private boolean placeEntities = true;

        private Transform transform = Transform.IDENTITY;

        private String paletteName = DEFAULT_PALETTE_NAME;

        public Builder addProcessor(BlockProcessor processor) {
            processors.add(processor);
            return this;
        }

        public Builder addNonNullProcessor() {
            return addProcessor(BlockProcessors.NON_NULL_PROCESSOR);
        }

        public Builder addRotationProcessor() {
            return addProcessor(BlockProcessors.ROTATION_PROCESSOR);
        }

        public Builder addFunction(String functionKey, FunctionProcessor processor) {
            this.functions.put(functionKey, processor);
            return this;
        }

        public StructurePlaceConfig build() {
            return new StructurePlaceConfig(this);
        }
    }
}