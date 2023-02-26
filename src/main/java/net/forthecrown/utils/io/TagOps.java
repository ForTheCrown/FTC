package net.forthecrown.utils.io;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CollectionTag;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.NumberTag;

public class TagOps implements DynamicOps<BinaryTag> {
  private static final Collector<Pair<String, BinaryTag>, CompoundTag, CompoundTag> MAP_COLLECTOR
      = Collector.of(
          BinaryTags::compoundTag,
          (tag, p) -> tag.put(p.getFirst(), p.getSecond()),
          (tag, tag2) -> {
            tag.merge(tag2);
            return tag;
          }
      );

  public static final TagOps OPS = new TagOps();

  private TagOps() {
  }

  @Override
  public BinaryTag empty() {
    return BinaryTags.endTag();
  }

  @Override
  public <U> U convertTo(DynamicOps<U> outOps, BinaryTag input) {
    return null;
  }

  @Override
  public DataResult<Number> getNumberValue(BinaryTag input) {
    if (input instanceof NumberTag numeric) {
      return DataResult.success(numeric.doubleValue());
    }

    return DataResult.error("Not a numeric element: " + input.toNbtString());
  }

  @Override
  public BinaryTag createByte(byte value) {
    return BinaryTags.byteTag(value);
  }

  @Override
  public BinaryTag createShort(short value) {
    return BinaryTags.shortTag(value);
  }

  @Override
  public BinaryTag createInt(int value) {
    return BinaryTags.intTag(value);
  }

  @Override
  public BinaryTag createLong(long value) {
    return BinaryTags.longTag(value);
  }

  @Override
  public BinaryTag createFloat(float value) {
    return BinaryTags.floatTag(value);
  }

  @Override
  public BinaryTag createDouble(double value) {
    return BinaryTags.doubleTag(value);
  }

  @Override
  public BinaryTag createIntList(IntStream input) {
    return BinaryTags.collectInts(input);
  }

  @Override
  public BinaryTag createLongList(LongStream input) {
    return BinaryTags.collectLongs(input);
  }

  @Override
  public BinaryTag createNumeric(Number i) {
    return BinaryTags.doubleTag(i.doubleValue());
  }

  @Override
  public DataResult<String> getStringValue(BinaryTag input) {
    if (input.isString()) {
      return DataResult.success(input.asString().value());
    }

    return DataResult.error("Not a string: " + input);
  }

  @Override
  public BinaryTag createString(String value) {
    return BinaryTags.stringTag(value);
  }

  @Override
  public DataResult<BinaryTag> mergeToList(BinaryTag list, BinaryTag value) {
    if (!(list instanceof CollectionTag t)) {
      return DataResult.error("Not a list: " + list);
    }

    if (!t.addTag(value)) {
      return Results.errorResult(
          "Element %s is not a matching type for %s list",
          value, list
      );
    }

    return DataResult.success(list);
  }

  @Override
  public DataResult<BinaryTag> mergeToMap(BinaryTag map,
                                          BinaryTag key,
                                          BinaryTag value
  ) {
    if (!map.isCompound()) {
      return DataResult.error("Not a map: " + map);
    }

    if (!key.isString()) {
      return DataResult.error("Not a string: " + key);
    }

    CompoundTag t1 = map.asCompound();
    String keyString = key.toString();

    CompoundTag result = BinaryTags.compoundTag(t1);
    result.put(keyString, value);
    return DataResult.success(result);
  }

  @Override
  public DataResult<Stream<Pair<BinaryTag, BinaryTag>>> getMapValues(
      BinaryTag input
  ) {
    if (!input.isCompound()) {
      return DataResult.error("Not a map: " + input);
    }

    return DataResult.success(
        input.asCompound()
            .entrySet()
            .stream()
            .map(e -> Pair.of(createString(e.getKey()), e.getValue()))
    );
  }

  @Override
  public BinaryTag createMap(Stream<Pair<BinaryTag, BinaryTag>> map) {
    return map.map(pair -> pair.mapFirst(Object::toString))
        .collect(MAP_COLLECTOR);
  }

  @Override
  public DataResult<Stream<BinaryTag>> getStream(BinaryTag input) {
    if (!(input instanceof CollectionTag t)) {
      return DataResult.error("Not a list: " + input);
    }

    Stream.Builder<BinaryTag> builder = Stream.builder();
    t.forEachTag(builder);
    return DataResult.success(builder.build());
  }

  @Override
  public BinaryTag createList(Stream<BinaryTag> input) {
    return input.collect(BinaryTags.tagCollector());
  }

  @Override
  public BinaryTag remove(BinaryTag input, String key) {
    if (input instanceof CompoundTag compoundTag) {
      var copy = BinaryTags.compoundTag(compoundTag);
      copy.remove(key);
      return copy;
    }

    return input;
  }
}