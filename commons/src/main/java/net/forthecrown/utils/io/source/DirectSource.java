package net.forthecrown.utils.io.source;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;

record DirectSource(CharSequence src, String name) implements Source {

  @Override
  public StringBuffer read() {
    return new StringBuffer(src);
  }

  @Override
  public <S> DataResult<S> save(DynamicOps<S> ops) {
    var builder = ops.mapBuilder();

    if (!Strings.isNullOrEmpty(name)) {
      builder.add("name", ops.createString(name));
    }

    return builder
        .add("raw", ops.createString(src.toString()))
        .build(ops.empty());
  }
}