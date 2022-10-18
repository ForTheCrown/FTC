package net.forthecrown.dungeons.level;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.dungeons.level.gate.GateData;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.Structures;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static net.forthecrown.dungeons.level.gate.GateData.TAG_CORRECT;
import static net.forthecrown.dungeons.level.gate.GateData.TAG_OPENING;

@RequiredArgsConstructor
public abstract class PieceType<T extends DungeonPiece> {
    @Getter
    private final String structureName;

    public abstract T create();
    public abstract T load(CompoundTag tag);

    public Optional<BlockStructure> getStructure() {
        return Structures.get()
                .getRegistry()
                .get(structureName);
    }

    public List<GateData> getGates() {
        return getStructure().map(structure -> {
            List<GateData> result = new ObjectArrayList<>();

            structure.getFunctions()
                    .stream()
                    // Filter non gate functions
                    .filter(info -> {
                        return switch (info.getFacing()) {
                            case SOUTH, NORTH, EAST, WEST -> info.getFunctionKey().equals(LevelFunctions.CONNECTOR);
                            default -> false;
                        };
                    })

                    // Load gate data
                    .map(info -> {
                        GateData.Opening opening = GateData.DEFAULT_OPENING;
                        Vector3i offset = info.getOffset();
                        boolean applyCorrection = true;

                        if (info.getTag() != null && !info.getTag().isEmpty()) {
                            var tag = info.getTag();
                            opening = GateData.Opening.load(tag.get(TAG_OPENING));

                            applyCorrection = !tag.contains(TAG_CORRECT)
                                    || tag.getBoolean(TAG_CORRECT);
                        }

                        if (applyCorrection) {
                            offset = offset.add(info.getFacing().getMod())
                                    .sub(0, 1, 0);
                        }

                        return new GateData(info.getFacing(), offset, opening);
                    })

                    // Populate list
                    .forEach(result::add);

            return result.isEmpty() ? null : result;
        }).orElse(Collections.emptyList());
    }
}