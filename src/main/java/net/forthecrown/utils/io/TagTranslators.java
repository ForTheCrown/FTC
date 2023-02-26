package net.forthecrown.utils.io;

import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.ByteArrayTag;
import net.forthecrown.nbt.ByteTag;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.DoubleTag;
import net.forthecrown.nbt.EndTag;
import net.forthecrown.nbt.FloatTag;
import net.forthecrown.nbt.IntArrayTag;
import net.forthecrown.nbt.IntTag;
import net.forthecrown.nbt.ListTag;
import net.forthecrown.nbt.LongArrayTag;
import net.forthecrown.nbt.LongTag;
import net.forthecrown.nbt.ShortTag;
import net.forthecrown.nbt.StringTag;
import net.forthecrown.nbt.TypeIds;
import net.minecraft.nbt.Tag;

public interface TagTranslators {

  TagTranslator<ByteTag, net.minecraft.nbt.ByteTag> BYTE
      = new TagTranslator<>() {
    @Override
    public net.minecraft.nbt.ByteTag toMinecraft(ByteTag apiType) {
      return net.minecraft.nbt.ByteTag.valueOf(apiType.byteValue());
    }

    @Override
    public ByteTag toApiType(net.minecraft.nbt.ByteTag minecraft) {
      return BinaryTags.byteTag(minecraft.getAsByte());
    }

    @Override
    public byte getId() {
      return TypeIds.BYTE;
    }
  };

  TagTranslator<ShortTag, net.minecraft.nbt.ShortTag> SHORT
      = new TagTranslator<>() {
    @Override
    public net.minecraft.nbt.ShortTag toMinecraft(ShortTag apiType) {
      return net.minecraft.nbt.ShortTag.valueOf(apiType.shortValue());
    }

    @Override
    public ShortTag toApiType(net.minecraft.nbt.ShortTag minecraft) {
      return BinaryTags.shortTag(minecraft.getAsShort());
    }

    @Override
    public byte getId() {
      return TypeIds.SHORT;
    }
  };

  TagTranslator<IntTag, net.minecraft.nbt.IntTag> INT
      = new TagTranslator<>() {
    @Override
    public net.minecraft.nbt.IntTag toMinecraft(IntTag apiType) {
      return net.minecraft.nbt.IntTag.valueOf(apiType.intValue());
    }

    @Override
    public IntTag toApiType(net.minecraft.nbt.IntTag minecraft) {
      return BinaryTags.intTag(minecraft.getAsInt());
    }

    @Override
    public byte getId() {
      return TypeIds.INT;
    }
  };

  TagTranslator<LongTag, net.minecraft.nbt.LongTag> LONG
      = new TagTranslator<>() {
    @Override
    public net.minecraft.nbt.LongTag toMinecraft(LongTag apiType) {
      return net.minecraft.nbt.LongTag.valueOf(apiType.longValue());
    }

    @Override
    public LongTag toApiType(net.minecraft.nbt.LongTag minecraft) {
      return BinaryTags.longTag(minecraft.getAsLong());
    }

    @Override
    public byte getId() {
      return TypeIds.LONG;
    }
  };

  TagTranslator<FloatTag, net.minecraft.nbt.FloatTag> FLOAT
      = new TagTranslator<>() {
    @Override
    public net.minecraft.nbt.FloatTag toMinecraft(FloatTag apiType) {
      return net.minecraft.nbt.FloatTag.valueOf(apiType.floatValue());
    }

    @Override
    public FloatTag toApiType(net.minecraft.nbt.FloatTag minecraft) {
      return BinaryTags.floatTag(minecraft.getAsFloat());
    }

    @Override
    public byte getId() {
      return TypeIds.FLOAT;
    }
  };

  TagTranslator<DoubleTag, net.minecraft.nbt.DoubleTag> DOUBLE
      = new TagTranslator<>() {
    @Override
    public net.minecraft.nbt.DoubleTag toMinecraft(DoubleTag apiType) {
      return net.minecraft.nbt.DoubleTag.valueOf(apiType.doubleValue());
    }

    @Override
    public DoubleTag toApiType(net.minecraft.nbt.DoubleTag minecraft) {
      return BinaryTags.doubleTag(minecraft.getAsDouble());
    }

    @Override
    public byte getId() {
      return TypeIds.DOUBLE;
    }
  };

  TagTranslator<ByteArrayTag, net.minecraft.nbt.ByteArrayTag> BYTE_ARRAY
      = new TagTranslator<>() {
    @Override
    public net.minecraft.nbt.ByteArrayTag toMinecraft(ByteArrayTag apiType) {
      return new net.minecraft.nbt.ByteArrayTag(apiType.toByteArray());
    }

    @Override
    public ByteArrayTag toApiType(net.minecraft.nbt.ByteArrayTag minecraft) {
      return BinaryTags.byteArrayTag(minecraft.getAsByteArray());
    }

    @Override
    public byte getId() {
      return TypeIds.BYTE_ARRAY;
    }
  };

  TagTranslator<IntArrayTag, net.minecraft.nbt.IntArrayTag> INT_ARRAY
      = new TagTranslator<>() {
    @Override
    public net.minecraft.nbt.IntArrayTag toMinecraft(IntArrayTag apiType) {
      return new net.minecraft.nbt.IntArrayTag(apiType.toIntArray());
    }

    @Override
    public IntArrayTag toApiType(net.minecraft.nbt.IntArrayTag minecraft) {
      return BinaryTags.intArrayTag(minecraft.getAsIntArray());
    }

    @Override
    public byte getId() {
      return TypeIds.INT_ARRAY;
    }
  };

  TagTranslator<LongArrayTag, net.minecraft.nbt.LongArrayTag> LONG_ARRAY
      = new TagTranslator<>() {
    @Override
    public net.minecraft.nbt.LongArrayTag toMinecraft(LongArrayTag apiType) {
      return new net.minecraft.nbt.LongArrayTag(apiType.toLongArray());
    }

    @Override
    public LongArrayTag toApiType(net.minecraft.nbt.LongArrayTag minecraft) {
      return BinaryTags.longArrayTag(minecraft.getAsLongArray());
    }

    @Override
    public byte getId() {
      return TypeIds.LONG_ARRAY;
    }
  };

  TagTranslator<ListTag, net.minecraft.nbt.ListTag> LIST
      = new TagTranslator<>() {
    @Override
    public net.minecraft.nbt.ListTag toMinecraft(ListTag apiType) {
      var list = new net.minecraft.nbt.ListTag();

      apiType.stream()
          .map(TagTranslators::toMinecraft)
          .forEach(list::add);

      return list;
    }

    @Override
    public ListTag toApiType(net.minecraft.nbt.ListTag minecraft) {
      return minecraft.stream()
          .map(TagTranslators::toApi)
          .collect(BinaryTags.tagCollector());
    }

    @Override
    public byte getId() {
      return TypeIds.LIST;
    }
  };

  TagTranslator<CompoundTag, net.minecraft.nbt.CompoundTag> COMPOUND
      = new TagTranslator<>() {
    @Override
    public net.minecraft.nbt.CompoundTag toMinecraft(CompoundTag apiType) {
      net.minecraft.nbt.CompoundTag t = new net.minecraft.nbt.CompoundTag();

      apiType.forEach((key, value) -> {
        t.put(key, TagTranslators.toMinecraft(value));
      });

      return t;
    }

    @Override
    public CompoundTag toApiType(net.minecraft.nbt.CompoundTag minecraft) {
      var tag = BinaryTags.compoundTag();

      minecraft.tags.forEach((s, tag1) -> {
        tag.put(s, TagTranslators.toApi(tag1));
      });

      return tag;
    }

    @Override
    public byte getId() {
      return TypeIds.COMPOUND;
    }
  };

  TagTranslator<StringTag, net.minecraft.nbt.StringTag> STRING
      = new TagTranslator<>() {
    @Override
    public net.minecraft.nbt.StringTag toMinecraft(StringTag apiType) {
      return net.minecraft.nbt.StringTag.valueOf(apiType.value());
    }

    @Override
    public StringTag toApiType(net.minecraft.nbt.StringTag minecraft) {
      return BinaryTags.stringTag(minecraft.getAsString());
    }

    @Override
    public byte getId() {
      return TypeIds.STRING;
    }
  };

  TagTranslator<EndTag, net.minecraft.nbt.EndTag> END
      = new TagTranslator<>() {
    @Override
    public net.minecraft.nbt.EndTag toMinecraft(EndTag apiType) {
      return net.minecraft.nbt.EndTag.INSTANCE;
    }

    @Override
    public EndTag toApiType(net.minecraft.nbt.EndTag minecraft) {
      return BinaryTags.endTag();
    }

    @Override
    public byte getId() {
      return TypeIds.END;
    }
  };

  TagTranslator[] TRANSLATORS = createTranslators();

  private static TagTranslator[] createTranslators() {
    TagTranslator[] translators = new TagTranslator[TypeIds.ID_COUNT];
    translators[END.getId()] = END;

    translators[BYTE.getId()] = BYTE;
    translators[SHORT.getId()] = SHORT;
    translators[INT.getId()] = INT;
    translators[LONG.getId()] = LONG;
    translators[FLOAT.getId()] = FLOAT;
    translators[DOUBLE.getId()] = DOUBLE;

    translators[STRING.getId()] = STRING;

    translators[BYTE_ARRAY.getId()] = BYTE_ARRAY;
    translators[INT_ARRAY.getId()] = INT_ARRAY;
    translators[LONG_ARRAY.getId()] = LONG_ARRAY;
    translators[LIST.getId()] = LIST;

    translators[COMPOUND.getId()] = COMPOUND;

    return translators;
  }

  static Tag toMinecraft(BinaryTag tag) {
    var translator = TRANSLATORS[tag.getId()];
    return translator.toMinecraft(tag);
  }

  static BinaryTag toApi(Tag tag) {
    var translator = TRANSLATORS[tag.getId()];
    return translator.toApiType(tag);
  }

  interface TagTranslator<API extends BinaryTag, NMS extends Tag> {
    NMS toMinecraft(API apiType);

    API toApiType(NMS minecraft);

    byte getId();
  }
}