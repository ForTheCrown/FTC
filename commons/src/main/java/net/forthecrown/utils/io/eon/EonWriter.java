package net.forthecrown.utils.io.eon;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import net.forthecrown.utils.io.parse.CharReader;
import net.forthecrown.utils.io.parse.StringEscaper;

public class EonWriter {

  private String indent;

  private int indentLevel = 0;
  private int depth = 0;

  private boolean pretty;

  private final Appendable out;

  public EonWriter(Appendable appendable) {
    this.out = appendable;
  }

  public void setIndent(String indent) {
    this.indent = indent;
    this.pretty = !Strings.isNullOrEmpty(indent);
  }

  public String getIndent() {
    return indent;
  }

  public void write(JsonElement element) throws IOException {
    if (element == null || element.isJsonNull()) {
      out.append("null");
      return;
    }

    if (element.isJsonArray()) {
      writeArray(element.getAsJsonArray());
      return;
    }

    if (element.isJsonObject()) {
      writeObject(element.getAsJsonObject());
      return;
    }

    JsonPrimitive primitive = element.getAsJsonPrimitive();

    if (primitive.isString()) {
      out.append(StringEscaper.toQuotedString(primitive.getAsString()));
      return;
    }

    if (primitive.isBoolean()) {
      boolean value = primitive.getAsBoolean();
      out.append(String.valueOf(value));
      return;
    }

    Number number = primitive.getAsNumber();
    String numString = number.toString();

    switch (numString) {
      case "Infinity" -> out.append("inf");
      case "-Infinity" -> out.append("-inf");
      case "NaN" -> out.append("nan");
      default -> out.append(number.toString());
    }
  }

  public void writeArray(JsonArray array) throws IOException {
    if (array.isEmpty()) {
      out.append("[]");
      return;
    }

    out.append('[');
    incIndent();

    var it = array.iterator();

    while (it.hasNext()) {
      var n = it.next();
      nlIndent();

      write(n);

      if (it.hasNext()) {
        out.append(',');
      }
    }

    decIndent();
    nlIndent();

    out.append(']');
  }

  public void writeObject(JsonObject object) throws IOException {
    if (object.size() == 0) {
      if (depth > 0) {
        out.append("{}");
      }

      return;
    }

    if (depth > 0) {
      out.append('{');
      incIndent();
    }

    depth++;

    var it = object.entrySet().iterator();
    boolean first = true;

    while (it.hasNext()) {
      var entry = it.next();
      String key = entry.getKey();
      JsonElement val = entry.getValue();

      if (!first || depth > 1) {
        nlIndent();
      }

      first = false;
      out.append(filterKey(key));

      if (pretty) {
        out.append(' ');
      }

      if (!val.isJsonObject() && !val.isJsonArray()) {
        out.append("=");

        if (pretty) {
          out.append(' ');
        }
      }
      write(val);
    }

    --depth;

    if (depth > 0) {
      decIndent();
      nlIndent();

      out.append('}');
    }
  }

  private static String filterKey(String s) {
    if (Strings.isNullOrEmpty(s)) {
      return "\"\"";
    }

    char[] chars = s.toCharArray();

    for (char aChar : chars) {
      if (!CharReader.IS_ID.test(aChar)) {
        return StringEscaper.toQuotedString(s);
      }
    }

    return s;
  }

  private void incIndent() {
    indentLevel++;
  }

  private void decIndent() {
    indentLevel--;
  }

  private void nlIndent() throws IOException {
    if (!pretty) {
      return;
    }

    out.append("\n").append(indent.repeat(indentLevel));
  }
}