package net.forthecrown.usables.conditions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.OptionalDynamic;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.text.Text;
import net.forthecrown.usables.BuiltType;
import net.forthecrown.usables.Condition;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.UsageType;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.io.FtcCodecs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;

public class TestCooldown implements Condition {

  public static final UsageType<TestCooldown> TYPE = BuiltType.<TestCooldown>builder()
      .parser((reader, source) -> new TestCooldown(ArgumentTypes.time().parse(reader)))
      .suggester(ArgumentTypes.time()::listSuggestions)

      .loader(dynamic -> {
        var stringResult = dynamic.asString();
        var numberResult = dynamic.asNumber();

        if (stringResult.result().isPresent()) {
          return FtcCodecs.DURATION.decode(dynamic)
              .map(Pair::getFirst)
              .map(TestCooldown::new);
        }

        if (numberResult.result().isPresent()) {
          return DataResult.success(
              new TestCooldown(Duration.ofMillis(numberResult.result().get().longValue()))
          );
        }

        OptionalDynamic<Object> dur = dynamic.get("duration");
        OptionalDynamic<Object> durMillis = dynamic.get("millisDuration");

        OptionalDynamic<Object> durationDyn = dur.get().result().isPresent() ? dur : durMillis;

        return durationDyn
            .flatMap(objectDynamic -> {
              return FtcCodecs.DURATION
                  .decode(objectDynamic.getOps(), objectDynamic.getValue())
                  .map(Pair::getFirst);
            })
            .map(TestCooldown::new)

            .map(cd -> {
              var entries = dynamic.get("entries").asMap(
                  objectDynamic -> UUID.fromString(objectDynamic.asString(null)),
                  objectDynamic -> objectDynamic.asNumber(null).longValue()
              );

              cd.cooldowns.putAll(entries);
              cd.clearExpired();

              return cd;
            });
      })

      .saver((value, ops) -> {
        value.clearExpired();

        var builder = ops.mapBuilder();
        builder.add("duration", FtcCodecs.DURATION.encodeStart(ops, value.duration));

        if (!value.cooldowns.isEmpty()) {
          var entries = ops.mapBuilder();

          value.cooldowns.forEach((uuid, timestamp) -> {
            entries.add(uuid.toString(), ops.createLong(timestamp));
          });

          builder.add("entries", entries.build(ops.empty()));
        }

        return builder.build(ops.empty());
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
  public UsageType<? extends UsableComponent> getType() {
    return TYPE;
  }

  @Override
  public @Nullable Component displayInfo() {
    return Text.format("{0, time}", duration);
  }
}
