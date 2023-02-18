package net.forthecrown.inventory.weapon.ability;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import net.forthecrown.grenadier.types.ArrayArgument;
import net.forthecrown.grenadier.types.TimeArgument;
import net.forthecrown.inventory.weapon.SwordRank;
import net.forthecrown.inventory.weapon.SwordRanks;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.format.PeriodFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.GenericMath;

@Getter
public class UpgradeCooldown implements ComponentLike {
  private static final ArrayArgument<Long> TIME_PARSER
      = ArrayArgument.of(TimeArgument.time());

  private final long min;
  private final long max;

  private UpgradeCooldown(long min, long max) {
    this.min = Math.min(max, min);
    this.max = Math.max(max, min);
  }

  public static UpgradeCooldown exact(long l) {
    return new UpgradeCooldown(l, l);
  }

  public static UpgradeCooldown between(long l1, long l2) {
    return new UpgradeCooldown(l1, l2);
  }

  public long getSize() {
    return max - min;
  }

  public boolean isExact() {
    return min == max;
  }

  public long get(SwordRank rank) {
    return _get(rank.getIndex());
  }

  private long _get(float r) {
    if (isExact()) {
      return min;
    }

    float max = SwordRanks.MAX_RANK;
    long size = getSize();
    return GenericMath.floorl(min + (size * ((max - r) / max)));
  }

  public long getCooldownChange() {
    long cooldown1 = _get(0);
    long cooldown2 = _get(1);

    return cooldown1 - cooldown2;
  }

  @Override
  public @NotNull Component asComponent() {
    long minMillis = Time.ticksToMillis(min);

    if (isExact()) {
      return PeriodFormat.of(minMillis)
          .withShortNames()
          .asComponent();
    }

    long maxMillis = Time.ticksToMillis(max);

    return Text.format("[{0, time, -short} to {1, time, -short}]",
        maxMillis, minMillis
    );
  }

  public static UpgradeCooldown read(JsonElement element) {
    if (element == null || element.isJsonNull()) {
      return null;
    }

    if (element.isJsonPrimitive()) {
      return exact(readTicks(element));
    } else if (!element.isJsonArray()) {
      throw new IllegalStateException(
          "Input is not Primitive nor Array: " + element
      );
    }

    JsonArray array = element.getAsJsonArray();

    if (array.size() != 2) {
      throw new IllegalStateException(
          "Expected size = 2 in range array, found: " + array.size()
      );
    }

    long min = readTicks(array.get(0));
    long max = readTicks(array.get(1));

    return between(min, max);
  }

  public static long readTicks(JsonElement element) {
    if (element == null || !element.isJsonPrimitive()) {
      return -1;
    }

    var prim = element.getAsJsonPrimitive();
    if (prim.isString()) {
      return parseTicks(prim.getAsString());
    }

    return prim.getAsLong();
  }

  public static long parseTicks(String input) {
    if (input.contains(":")) {
      LocalTime localTime = LocalTime.parse(input);
      long nano = localTime.toNanoOfDay();
      return Time.millisToTicks(TimeUnit.NANOSECONDS.toMillis(nano));
    }

    try {
      long time = TIME_PARSER.parse(new StringReader(input))
          .stream()
          .mapToLong(value -> value)
          .sum();

      return Time.millisToTicks(time);
    } catch (CommandSyntaxException exc) {
      throw new IllegalStateException(exc);
    }
  }
}