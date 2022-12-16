package net.forthecrown.dungeons.level.generator;

import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.dungeons.DungeonManager;
import net.forthecrown.dungeons.level.Pieces;
import net.forthecrown.dungeons.level.RoomType;
import org.apache.commons.lang3.Range;

import java.util.stream.Stream;

import static net.forthecrown.dungeons.level.generator.PieceGenerator.DEFAULT_WEIGHT;

public enum SectionType {
    /* ----------------------------- CONNECTOR TYPE ------------------------------ */

    CONNECTOR {
        @Override
        public Range<Integer> createDepth(TreeGeneratorConfig config) {
            return Range.between(config.getMinConnectorDepth(), config.getMaxConnectorDepth());
        }

        @Override
        public Stream<IntObjectPair<RoomType>> fillPotentials(PieceGenerator gen) {
            return DungeonManager.getInstance().getRoomTypes()
                    .stream()

                    .filter(holder -> holder.getValue().hasFlags(Pieces.FLAG_CONNECTOR))

                    .map(Holder::getValue)
                    .map(type -> {
                        int weight = DEFAULT_WEIGHT;
                        int gateAmount = type.getGates().size();

                        weight += (gen.getDepth() < gen.getConfig().getMinDepth()) ? gateAmount : -gateAmount;
                        weight += (gateAmount - 1) <= gen.getConfig().getMaxConnectorExits() ?
                                gateAmount * 2 : -(gateAmount * 2);

                        return IntObjectPair.of(weight, type);
                    });
        }

        @Override
        public int getMaxExits(TreeGeneratorConfig config) {
            return config.getMaxConnectorExits();
        }
    },

    /* ----------------------------- ROOM TYPE ------------------------------ */

    ROOM {
        @Override
        public Range<Integer> createDepth(TreeGeneratorConfig config) {
            return Range.between(config.getMinRoomDepth(), config.getMaxRoomDepth());
        }

        @Override
        public Stream<IntObjectPair<RoomType>> fillPotentials(PieceGenerator gen) {
            return DungeonManager.getInstance().getRoomTypes()
                    .stream()

                    .filter(holder -> !holder.getValue().hasFlags(Pieces.FLAG_CONNECTOR))

                    .map(Holder::getValue)
                    .map(type -> {
                        int weight = DEFAULT_WEIGHT;
                        int gateAmount = type.getGates().size();

                        weight += (gateAmount - 1) <= gen.getConfig().getMaxRoomExits() ?
                                gateAmount * 2 : -(gateAmount * 2);

                        return IntObjectPair.of(weight, type);
                    });
        }

        @Override
        public int getMaxExits(TreeGeneratorConfig config) {
            return config.getMaxRoomExits();
        }
    };

    private static final SectionType[] VALUES = values();

    public SectionType next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public abstract Range<Integer> createDepth(TreeGeneratorConfig config);

    public abstract Stream<IntObjectPair<RoomType>> fillPotentials(PieceGenerator gen);

    public abstract int getMaxExits(TreeGeneratorConfig config);
}