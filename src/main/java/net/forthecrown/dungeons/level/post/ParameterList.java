package net.forthecrown.dungeons.level.post;

import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import net.forthecrown.core.Crown;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.math.GenericMath;

public class ParameterList {
    private static final Registry<PostParameter> PARAMETERS = Registries.ofEnum(PostParameter.class);

    private double[] values = ArrayUtils.EMPTY_DOUBLE_ARRAY;

     public void set(PostParameter param, double value) {
         values = DoubleArrays.ensureCapacity(values, param.ordinal() + 1);
         values[param.ordinal()] = GenericMath.clamp(value, 0.0D, 1.0D);
     }

     public double get(PostParameter param) {
         if (values.length <= param.ordinal()) {
             return param.getDefaultValue();
         }

         return values[param.ordinal()];
     }

     public CompoundTag save() {
         CompoundTag result = new CompoundTag();

         for (int i = 0; i < values.length; i++) {
             var param = PARAMETERS.getHolder(i).orElseThrow();
             double value = values[i];

             if (value == param.getValue().getDefaultValue()) {
                 continue;
             }

             result.putDouble(param.getKey(), value);
         }

         return result;
     }

     public void load(Tag t) {
         CompoundTag tag = (CompoundTag) t;

         for (var e: tag.tags.entrySet()) {
             var paramOptional = PARAMETERS.get(e.getKey());

             if (paramOptional.isEmpty()) {
                 Crown.logger().error("Unknown post parameter='{}'", e.getKey());
                 continue;
             }

             var param = paramOptional.get();
             double d = ((DoubleTag) e.getValue()).getAsDouble();

             set(param, d);
         }
     }
}