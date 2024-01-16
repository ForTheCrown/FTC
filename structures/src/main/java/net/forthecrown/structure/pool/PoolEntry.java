package net.forthecrown.structure.pool;

import com.google.common.base.Strings;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.forthecrown.registry.Registries;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.utils.io.Results;

public record PoolEntry(int weight, String structureName, String paletteName) {

  public static Pattern SPLIT_PATTERN = Pattern.compile(
      "(" + Registries.VALID_KEY_REGEX + ")"
          + "(?:::(" + Registries.VALID_KEY_REGEX + "))"
  );

  private static final Codec<PoolEntry> STRING_CODEC = Codec.STRING.comapFlatMap(
      string -> {
        Matcher matcher = SPLIT_PATTERN.matcher(string);
        if (!matcher.matches()) {
          return Results.error("Invalid structure reference: '%s'", string);
        }

        String struct = matcher.group(1);
        String palette = matcher.group(2);

        return Results.success(new PoolEntry(1, struct, palette));
      },
      poolEntry -> {
        if (Strings.isNullOrEmpty(poolEntry.paletteName)) {
          return poolEntry.structureName;
        }

        return poolEntry.structureName + "::" + poolEntry.paletteName;
      }
  );

  private static final Codec<PoolEntry> RECORD_CODEC = RecordCodecBuilder.create(instance -> {
    return instance
        .group(
            Codec.INT.optionalFieldOf("weight", 1).forGetter(o -> o.weight),
            Codec.STRING.fieldOf("structure").forGetter(o -> o.structureName),
            Codec.STRING.optionalFieldOf("palette", BlockStructure.DEFAULT_PALETTE_NAME)
                .forGetter(o -> o.paletteName)
        )
        .apply(instance, PoolEntry::new);
  });

  public static final Codec<PoolEntry> CODEC = Codec.either(STRING_CODEC, RECORD_CODEC)
      .xmap(
          e -> e.map(Function.identity(), Function.identity()),
          poolEntry -> {
            if (poolEntry.isSimple()) {
              return Either.left(poolEntry);
            }
            return Either.right(poolEntry);
          }
      );

  public boolean isSimple() {
    return weight == 1;
  }
}
