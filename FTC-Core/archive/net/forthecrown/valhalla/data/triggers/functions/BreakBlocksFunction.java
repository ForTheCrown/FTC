package net.forthecrown.valhalla.data.triggers.functions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.serializer.SerializerType;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.math.FtcBoundingBox;
import net.forthecrown.valhalla.Valhalla;
import net.forthecrown.valhalla.active.ActiveRaid;
import net.kyori.adventure.key.Key;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BreakBlocksFunction implements TriggerFunction {
    public static final Key KEY = Valhalla.vikingKey("break_blocks");

    public static final SerializerType<BreakBlocksFunction> TYPE = new SerializerType<>() {
        @Override
        public BreakBlocksFunction deserialize(JsonElement element) {
            JsonObject json = element.getAsJsonObject();

            if(json.has("minX")) {
                return new BreakBlocksFunction(null, JsonUtils.readBoundingBox(json));
            } else {
                BoundingBox box = JsonUtils.readBoundingBox(json.get("region").getAsJsonObject());
                BlockPredicate predicate = BlockPredicate.fromJson(json.get("predicate"));

                return new BreakBlocksFunction(predicate, box);
            }
        }

        @Override
        public JsonElement serialize(BreakBlocksFunction value) {
            JsonObject box = JsonUtils.writeBoundingBox(value.region);

            if(value.predicate == null) return box;
            else {
                JsonObject json = new JsonObject();

                json.add("region", box);
                json.add("predicate", value.predicate.serializeToJson());

                return json;
            }
        }

        @Override
        public @NotNull Key key() {
            return KEY;
        }
    };

    private final BlockPredicate predicate;
    private final BoundingBox region;

    public BreakBlocksFunction(@Nullable BlockPredicate predicate, @NotNull BoundingBox region) {
        this.predicate = predicate;
        this.region = Validate.notNull(region);
    }

    @Override
    public void execute(ActiveRaid raid) {
        FtcBoundingBox region = FtcBoundingBox.of(raid.getRegion().getWorld(), this.region);
        ServerLevel level = ((CraftWorld) region.getWorld()).getHandle();

        for (Block b: region) {
            BlockPos pos = new BlockPos(b.getX(), b.getY(), b.getZ());

            if(predicate == null || predicate.matches(level, pos)) b.breakNaturally();
        }
    }

    @Override
    public SerializerType<BreakBlocksFunction> serializerType() {
        return TYPE;
    }
}
