package net.forthecrown.utils.io.source;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;

record DirectSource(CharSequence src, String name) implements Source {

  @Override
  public StringBuffer read() {
    return new StringBuffer(src);
  }

  @Override
  public JsonElement save() {
    JsonObject obj = new JsonObject();

    obj.addProperty("raw", src.toString());

    if (!name.equals("<eval>")) {
      obj.addProperty("name", name);
    }

    return obj;
  }


  @Override
  public BinaryTag saveAsTag() {
    CompoundTag tag = BinaryTags.compoundTag();

    String stringUrl = src.toString();
    tag.putString("raw", stringUrl);

    if (!name.equals("<eval>")) {
      tag.putString("name", name);
    }

    return tag;
  }
}