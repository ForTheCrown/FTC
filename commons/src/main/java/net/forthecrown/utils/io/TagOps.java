package net.forthecrown.utils.io;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.nio.ByteBuffer;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.ByteArrayTag;
import net.forthecrown.nbt.ByteTag;
import net.forthecrown.nbt.CollectionTag;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.DoubleTag;
import net.forthecrown.nbt.FloatTag;
import net.forthecrown.nbt.IntArrayTag;
import net.forthecrown.nbt.IntTag;
import net.forthecrown.nbt.ListTag;
import net.forthecrown.nbt.LongArrayTag;
import net.forthecrown.nbt.LongTag;
import net.forthecrown.nbt.NumberTag;
import net.forthecrown.nbt.ShortTag;
import net.forthecrown.nbt.StringTag;

public class TagOps implements DynamicOps<BinaryTag> {
  private static final Collector<Pair<String, BinaryTag>, CompoundTag, CompoundTag> MAP_COLLECTOR
      = Collector.of(
          BinaryTags::compoundTag,
          (tag, p) -> tag.put(p.getFirst(), p.getSecond()),
          CompoundTag::merge
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
    if (input instanceof ListTag list) {
      return outOps.createList(
          list.stream().map(tag -> convertTo(outOps, tag))
      );
    }

    if (input instanceof ByteArrayTag arr) {
      ByteBuffer buf = ByteBuffer.wrap(arr.toByteArray());
      return outOps.createByteList(buf);
    }

    if (input instanceof IntArrayTag arr) {
      return outOps.createIntList(arr.intStream());
    }

    if (input instanceof LongArrayTag arr) {
      return outOps.createLongList(arr.longStream());
    }

    if (input instanceof CompoundTag tag) {
      return convertMap(outOps, tag);
    }

    if (input instanceof StringTag str) {
      return outOps.createString(str.value());
    }

    if (input instanceof ByteTag t) {
      return outOps.createByte(t.byteValue());
    }

    if (input instanceof ShortTag t) {
      return outOps.createShort(t.shortValue());
    }

    if (input instanceof IntTag t) {
      return outOps.createInt(t.intValue());
    }

    if (input instanceof LongTag t) {
      return outOps.createLong(t.longValue());
    }

    if (input instanceof FloatTag t) {
      return outOps.createFloat(t.floatValue());
    }

    if (input instanceof DoubleTag t) {
      return outOps.createDouble(t.doubleValue());
    }

    return outOps.empty();
  }

  @Override
  public DataResult<Number> getNumberValue(BinaryTag input) {
    if (input instanceof NumberTag numeric) {
      return Results.success(numeric.doubleValue());
    }

    return Results.error("Not a numeric element: " + input.toNbtString());
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
      return Results.success(input.asString().value());
    }

    return Results.error("Not a string: " + input);
  }

  @Override
  public BinaryTag createString(String value) {
    return BinaryTags.stringTag(value);
  }

  @Override
  public DataResult<BinaryTag> mergeToList(BinaryTag list, BinaryTag value) {
    if (!(list instanceof CollectionTag t)) {
      return Results.error("Not a list: " + list);
    }

    var listTag = t.copy();
    if (!listTag.addTag(value)) {
      return Results.error(
          "Element %s is not a matching type for %s list",
          value, list
      );
    }

    return Results.success(listTag);
  }

  @Override
  public DataResult<BinaryTag> mergeToMap(BinaryTag map,
                                          BinaryTag key,
                                          BinaryTag value
  ) {
    if (!map.isCompound()) {
      return Results.error("Not a map: " + map);
    }

    if (!key.isString()) {
      return Results.error("Not a string: " + key);
    }

    CompoundTag t1 = map.asCompound();
    String keyString = key.toString();

    CompoundTag result = t1.copy();
    result.put(keyString, value);
    return Results.success(result);
  }

  @Override
  public DataResult<Stream<Pair<BinaryTag, BinaryTag>>> getMapValues(
      BinaryTag input
  ) {
    if (!input.isCompound()) {
      return Results.error("Not a map: " + input);
    }

    return Results.success(
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
      return Results.error("Not a list: " + input);
    }

    Stream.Builder<BinaryTag> builder = Stream.builder();
    t.forEachTag(builder);
    return Results.success(builder.build());
  }

  @Override
  public BinaryTag createList(Stream<BinaryTag> input) {
    return input.collect(BinaryTags.tagCollector());
  }

  @Override
  public BinaryTag remove(BinaryTag input, String key) {
    if (input instanceof CompoundTag compoundTag) {
      var copy = compoundTag.copy();
      copy.remove(key);
      return copy;
    }

    return input;
  }
}