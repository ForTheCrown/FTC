package net.forthecrown.usables;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.forthecrown.grenadier.types.IntRangeArgument.IntRange;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

public class UsableCodecs {

  public static final Codec<IntRange> INT_RANGE = new Codec<>() {

    @Override
    public <T> DataResult<Pair<IntRange, T>> decode(DynamicOps<T> ops, T input) {
      Mutable<Integer> min = new MutableObject<>(null);
      Mutable<Integer> max = new MutableObject<>(null);

      ops.get(input, "min").flatMap(ops::getNumberValue).result().ifPresent(number -> {
        min.setValue(number.intValue());
      });

      ops.get(input, "max").flatMap(ops::getNumberValue).result().ifPresent(number -> {
        max.setValue(number.intValue());
      });

      return DataResult.success(IntRange.range(min.getValue(), max.getValue()))
          .map(intRange -> Pair.of(intRange, input));
    }

    @Override
    public <T> DataResult<T> encode(IntRange value, DynamicOps<T> ops, T prefix) {
      var mapBuilder = ops.mapBuilder();

      value.min().ifPresent(value1 -> {
        mapBuilder.add("min", ops.createInt(value1));
      });

      value.max().ifPresent(value1 -> {
        mapBuilder.add("max", ops.createInt(value1));
      });

      return mapBuilder.build(prefix);
    }
  };
}
