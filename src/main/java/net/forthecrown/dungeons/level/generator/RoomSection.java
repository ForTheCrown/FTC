package net.forthecrown.dungeons.level.generator;

import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.dungeons.level.RoomType;
import net.forthecrown.dungeons.level.Rooms;
import net.forthecrown.dungeons.level.gate.DungeonGate;
import org.apache.commons.lang3.Range;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class RoomSection extends SectionGenerator<RoomSection> {
    public RoomSection(TreeGenerator gen, DungeonGate origin, @Nullable SectionGenerator parent) {
        super(gen, origin, parent);
    }

    @Override
    protected Stream<IntObjectPair<RoomType>> fillPotentials() {
        return Rooms.REGISTRY
                .entries()
                .stream()

                .filter(holder -> {
                    int exits = holder.getValue().getGates().size() - 1;
                    if (exits > gen.getConfig().getMaxRoomExits()) {
                        return false;
                    }

                    var parentRoom = origin.getParent();

                    if (holder.getValue().equals(parentRoom.getType())) {
                        return false;
                    }

                    var type = holder.getValue();

                    // Cannot be connector, root or boss room, done by ensuring that
                    // the given type does not have any of the aforementioned flags
                    // set
                    return (type.getFlags() & (Rooms.FLAG_CONNECTOR | Rooms.FLAG_ROOT | Rooms.FLAG_BOSS_ROOM)) == 0;
                })

                .map(Holder::getValue)
                .map(type -> IntObjectPair.of(10, type));
    }

    @Override
    protected RoomSection createCopy(DungeonGate gate) {
        return new RoomSection(gen, gate, this);
    }

    @Override
    protected Range<Integer> createDepthRange(TreeGeneratorConfig config) {
        return Range.between(config.getMinRoomDepth(), config.getMaxRoomDepth());
    }
}