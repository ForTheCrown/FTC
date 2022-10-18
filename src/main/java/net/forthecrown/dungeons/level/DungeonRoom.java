package net.forthecrown.dungeons.level;

import lombok.Getter;
import net.forthecrown.dungeons.level.post.ParameterList;
import net.forthecrown.structure.FunctionInfo;
import net.forthecrown.structure.FunctionProcessor;
import net.forthecrown.structure.StructurePlaceConfig;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

@Getter
public class DungeonRoom extends DungeonPiece {
    private final ParameterList parameters = new ParameterList();

    public DungeonRoom(RoomType type) {
        super(type);
    }

    public DungeonRoom(RoomType piece, CompoundTag tag) {
        super(piece, tag);

    }

    @Override
    public RoomType getType() {
        return (RoomType) super.getType();
    }

    @Override
    protected StructurePlaceConfig.Builder createPlaceConfig(World world) {
        return super.createPlaceConfig(world)
                .addFunction(LevelFunctions.TREASURE, TreasureProcessor.INSTANCE);
    }

    @Override
    protected PieceVisitor.Result onVisit(PieceVisitor walker) {
        return walker.onRoom(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {}

    public enum TreasureProcessor implements FunctionProcessor {
        INSTANCE;

        public static final String
                TAG_LOOTTABLE = "lootTable",
                TAG_TYPE = "block_type";

        @Override
        public void process(@NotNull FunctionInfo info, @NotNull StructurePlaceConfig config) {
            if (info.getTag() == null
                    || info.getTag().isEmpty()
                    || !info.getTag().contains(TAG_LOOTTABLE)
            ) {
                return;
            }


        }
    }
}