package net.forthecrown.dungeons.level.generator;

import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.dungeons.level.RoomType;
import net.forthecrown.dungeons.level.Rooms;
import net.forthecrown.dungeons.level.gate.DungeonGate;
import org.apache.commons.lang3.Range;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class PathSection extends SectionGenerator<PathSection> {
    public PathSection(TreeGenerator gen, @Nullable DungeonGate origin, @Nullable SectionGenerator parent) {
        super(gen, origin, parent);
    }

    @Override
    protected Stream<IntObjectPair<RoomType>> fillPotentials() {
        return Rooms.REGISTRY
                .entries()
                .stream()

                .filter(holder -> {
                    int exits = holder.getValue().getGates().size() - 1;
                    if (exits > gen.getConfig().getMaxConnectorExits()) {
                        return false;
                    }

                    var parentRoom = origin.getParent();

                    if (holder.getValue().equals(parentRoom.getType())) {
                        return false;
                    }

                    var type = holder.getValue();
                    return type.hasFlags(Rooms.FLAG_CONNECTOR);
                })

                .map(Holder::getValue)
                .map(type -> {
                    int weight = DEFAULT_WEIGHT;
                    int gateAmount = type.getGates().size();

                    if (depth < config.getMinDepth()) {
                        weight += gateAmount;
                    } else {
                        weight -= gateAmount;
                    }

                    return IntObjectPair.of(weight, type);
                });
    }

    @Override
    protected Range<Integer> createDepthRange(TreeGeneratorConfig config) {
        return Range.between(config.getMinConnectorDepth(), config.getMaxConnectorDepth());
    }

    @Override
    protected PathSection createCopy(DungeonGate gate) {
        return new PathSection(gen, gate, this);
    }
}