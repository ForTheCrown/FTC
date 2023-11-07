package net.forthecrown.utils.io.source;

import static net.forthecrown.utils.io.source.Sources.CHARSET;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Objects;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;

public record UrlSource(URL url, String name) implements Source {

  @Override
  public StringBuffer read() throws IOException {
    InputStream stream = url.openStream();
    InputStreamReader reader = new InputStreamReader(stream, CHARSET);
    StringWriter strWriter = new StringWriter();

    reader.transferTo(strWriter);
    StringBuffer buf = strWriter.getBuffer();
    stream.close();

    return buf;
  }

  @Override
  public <S> DataResult<S> save(DynamicOps<S> ops) {
    var builder = ops.mapBuilder();
    var stringUrl = url.toString();

    builder.add("url", ops.createString(stringUrl));

    if (name != null && !Objects.equals(name, stringUrl)) {
      builder.add("name", ops.createString(name));
    }

    return builder.build(ops.empty());
  }
}