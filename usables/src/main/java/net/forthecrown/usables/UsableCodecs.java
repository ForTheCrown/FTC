package net.forthecrown.usables;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.OptionalInt;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.IntRangeArgument.IntRange;
import net.forthecrown.utils.io.FtcCodecs;

public class UsableCodecs {

  private static Codec<IntRange> RECORD_CODEC = RecordCodecBuilder.create(instance -> {
    return instance
        .group(
            Codec.INT.optionalFieldOf("min").forGetter(o -> toOpt(o.min())),
            Codec.INT.optionalFieldOf("max").forGetter(o -> toOpt(o.max()))
        )
        .apply(instance, (min, max) -> {
          return IntRange.range(min.orElse(null), max.orElse(null));
        });
  });

  public static final Codec<IntRange> INT_RANGE = new Codec<>() {

    @Override
    public <T> DataResult<Pair<IntRange, T>> decode(DynamicOps<T> ops, T input) {
      var strResult = ops.getStringValue(input);
      var intResult = ops.getNumberValue(input).map(Number::intValue);

      if (strResult.result().isPresent()) {
        return strResult
            .flatMap(s -> FtcCodecs.safeParse(s, ArgumentTypes.intRange()))
            .map(range -> Pair.of(range, input));
      }

      if (intResult.result().isPresent()) {
        return intResult
            .map(IntRange::exactly)
            .map(range -> Pair.of(range, input));
      }

      return RECORD_CODEC.decode(ops, input);
    }

    @Override
    public <T> DataResult<T> encode(IntRange value, DynamicOps<T> ops, T prefix) {
      if (value.isExact()) {
        return DataResult.success(ops.createInt(value.min().orElse(0)));
      }

      return DataResult.success(ops.createString(value.toString()));
    }
  };

  private static Optional<Integer> toOpt(OptionalInt o) {
    return o.isPresent() ? Optional.of(o.getAsInt()) : Optional.empty();
  }
}
