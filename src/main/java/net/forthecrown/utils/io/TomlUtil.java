package net.forthecrown.utils.io;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.time.temporal.TemporalAccessor;
import org.tomlj.TomlArray;
import org.tomlj.TomlTable;

public final class TomlUtil {
  private TomlUtil() {}

  public static <E extends Enum<E>> E readEnum(Object o, Class<E> type) {
    return readEnum(o, type, null);
  }

  public static <E extends Enum<E>> E readEnum(Object o, Class<E> type, E def) {
    if (!(o instanceof String str)) {
      return def;
    }

    return Enum.valueOf(type, str.toUpperCase());
  }

  public static JsonObject toJson(TomlTable table) {
    JsonObject obj = new JsonObject();

    for (var e: table.entrySet()) {
      obj.add(e.getKey(), _toJson(e.getValue()));
    }

    return obj;
  }

  public static JsonArray toJson(TomlArray array) {
    JsonArray jArray = new JsonArray();

    for (int i = 0; i < array.size(); i++) {
      jArray.add(_toJson(array.get(i)));
    }

    return jArray;
  }

  private static JsonElement _toJson(Object o) {
    // Primitives
    if (o instanceof Boolean b) {
      return new JsonPrimitive(b);
    } else if (o instanceof String str) {
      return new JsonPrimitive(str);
    } else if (o instanceof Number number) {
      return new JsonPrimitive(number);
    } else if (o instanceof Character c) {
      return new JsonPrimitive(c);
    }

    // Containers
    if (o instanceof TomlTable table) {
      return toJson(table);
    } else if (o instanceof TomlArray arr) {
      return toJson(arr);
    }

    // Date/Time objects, they all implement TemporalAccessor
    if (o instanceof TemporalAccessor) {
      return new JsonPrimitive(o.toString());
    }

    throw new IllegalStateException("Unexpected class type: " + o);
  }
}