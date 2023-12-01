package net.forthecrown.structure.pool;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.forthecrown.utils.io.FtcCodecs;

public record PoolEntry(int weight, String structureName) {

  private static final Codec<PoolEntry> STRING_CODEC
      = FtcCodecs.KEY_CODEC.xmap(string -> new PoolEntry(1, string), PoolEntry::structureName);

  private static final Codec<PoolEntry> RECORD_CODEC = RecordCodecBuilder.create(instance -> {
    return instance
        .group(
            Codec.INT.optionalFieldOf("weight", 1).forGetter(o -> o.weight),
            Codec.STRING.fieldOf("structure").forGetter(o -> o.structureName)
        )
        .apply(instance, PoolEntry::new);
  });

  public static final Codec<PoolEntry> CODEC = Codec.either(STRING_CODEC, RECORD_CODEC)
      .xmap(
          e -> e.map(Function.identity(), Function.identity()),
          poolEntry -> {
            if (poolEntry.weight == 1) {
              return Either.left(poolEntry);
            }
            return Either.right(poolEntry);
          }
      );
}
