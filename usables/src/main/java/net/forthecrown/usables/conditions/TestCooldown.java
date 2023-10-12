package net.forthecrown.usables.conditions;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.EitherMapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.text.Text;
import net.forthecrown.usables.BuiltType;
import net.forthecrown.usables.Condition;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.ObjectType;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.TomlConfigs;
import net.forthecrown.utils.io.FtcCodecs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;

public class TestCooldown implements Condition {

  public static final Codec<Object2LongMap<UUID>> CD_MAP_CODEC
      = Codec.unboundedMap(FtcCodecs.STRING_UUID, Codec.LONG)
      .xmap(Object2LongOpenHashMap::new, Function.identity());

  private static final MapCodec<Duration> DURATION_EITHER_CODEC
      = new EitherMapCodec<>(
          FtcCodecs.DURATION.fieldOf("millisDuration"),
          FtcCodecs.DURATION.fieldOf("duration")
      )
      .xmap(e -> e.map(d1 -> d1, d2 -> d2), Either::right);

  public static final Codec<TestCooldown> RECORD_CODEC = RecordCodecBuilder.create(instance -> {
    return instance
        .group(
            DURATION_EITHER_CODEC.forGetter(o -> o.duration),
            CD_MAP_CODEC.optionalFieldOf("entries").forGetter(o -> Optional.of(o.cooldowns))
        )
        .apply(instance, (duration, entries) -> {
          var cd = new TestCooldown(duration);
          cd.cooldowns.putAll(entries.orElseGet(Object2LongMaps::emptyMap));
          return cd;
        });
  });

  public static final Codec<TestCooldown> CODEC = Codec.of(
      RECORD_CODEC,

      new Decoder<>() {
        @Override
        public <T> DataResult<Pair<TestCooldown, T>> decode(DynamicOps<T> ops, T input) {
          var strResult = ops.getStringValue(input);
          var numResult = ops.getNumberValue(input);

          if (numResult.result().isPresent()) {
            return numResult
                .map(number -> Duration.ofMillis(number.longValue()))
                .map(TestCooldown::new)
                .map(testCooldown -> new Pair<>(testCooldown, input));
          }

          if (strResult.result().isPresent()) {
            return strResult
                .flatMap(s -> FtcCodecs.safeParse(s, TomlConfigs::parseDuration))
                .map(duration1 -> new Pair<>(new TestCooldown(duration1), input));
          }

          return RECORD_CODEC.decode(ops, input);
        }
      }
  );

  public static final ObjectType<TestCooldown> TYPE = BuiltType.<TestCooldown>builder()
      .parser((reader, source) -> new TestCooldown(ArgumentTypes.time().parse(reader)))
      .suggester(ArgumentTypes.time()::listSuggestions)

      .loader(CODEC::parse)

      .saver((value, ops) -> {
        value.clearExpired();
        return CODEC.encodeStart(ops, value);
      })

      .build();

  private final Duration duration;
  private final Object2LongMap<UUID> cooldowns = new Object2LongOpenHashMap<>();

  public TestCooldown(Duration duration) {
    this.duration = duration;
  }

  private void clearExpired() {
    cooldowns.values().removeIf(Time::isPast);
  }

  @Override
  public boolean test(Interaction interaction) {
    clearExpired();
    return !cooldowns.containsKey(interaction.playerId());
  }

  @Override
  public void afterTests(Interaction interaction) {
    long end = System.currentTimeMillis() + duration.toMillis();
    cooldowns.put(interaction.playerId(), end);
  }

  @Override
  public Component failMessage(Interaction interaction) {
    if (duration.toMillis() < TimeUnit.MINUTES.toMillis(2)
        || !cooldowns.containsKey(interaction.playerId())
    ) {
      return Component.text("You're on cooldown!", NamedTextColor.GRAY);
    }

    return Text.format("You cannot use this for {0, time, -timestamp}",
        NamedTextColor.GRAY,
        cooldowns.getLong(interaction.playerId())
    );
  }

  @Override
  public ObjectType<? extends UsableComponent> getType() {
    return TYPE;
  }

  @Override
  public @Nullable Component displayInfo() {
    return Text.format("{0, time}", duration);
  }
}
