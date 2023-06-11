package net.forthecrown.utils.io.source;

import static net.forthecrown.utils.io.source.Sources.CHARSET;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
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
  public JsonElement save() {
    JsonObject obj = new JsonObject();

    String stringUrl = url.toString();
    obj.addProperty("url", stringUrl);

    if (!name.equals(stringUrl)) {
      obj.addProperty("name", stringUrl);
    }

    return obj;
  }

  @Override
  public BinaryTag saveAsTag() {
    CompoundTag tag = BinaryTags.compoundTag();

    String stringUrl = url.toString();
    tag.putString("url", stringUrl);

    if (!name.equals(stringUrl)) {
      tag.putString("name", name);
    }

    return tag;
  }
}