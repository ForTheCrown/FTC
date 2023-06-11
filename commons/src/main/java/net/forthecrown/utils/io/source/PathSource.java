package net.forthecrown.utils.io.source;

import static net.forthecrown.utils.io.source.Sources.CHARSET;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;

record PathSource(Path path, String name) implements Source {

  @Override
  public StringBuffer read() throws IOException {
    BufferedReader reader = Files.newBufferedReader(path, CHARSET);
    StringWriter writer = new StringWriter();
    reader.transferTo(writer);
    reader.close();

    return writer.getBuffer();
  }

  @Override
  public JsonElement save() {
    JsonObject obj = new JsonObject();

    String stringUrl = path.toString();
    obj.addProperty("path", stringUrl);

    if (!name.equals(stringUrl)) {
      obj.addProperty("name", stringUrl);
    }

    return obj;
  }


  @Override
  public BinaryTag saveAsTag() {
    CompoundTag tag = BinaryTags.compoundTag();

    String stringUrl = path.toString();
    tag.putString("path", stringUrl);

    if (!name.equals(stringUrl)) {
      tag.putString("name", name);
    }

    return tag;
  }
}