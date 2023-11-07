package net.forthecrown.utils.io.configurate;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.RepresentationHint;
import org.tomlj.Toml;

class TomlSerializer {

  private final BufferedWriter out;
  private int indent;

  public TomlSerializer(BufferedWriter out) {
    this.out = out;
  }

  public static void write(ConfigurationNode node, Writer writer) throws IOException {
    BufferedWriter buffered = writer instanceof BufferedWriter buf
        ? buf
        : new BufferedWriter(writer);

    TomlSerializer serializer = new TomlSerializer(buffered);
    serializer.writeNode(node, false);
  }

  private void writeIndent() throws IOException {
    if (indent <= 0) {
      return;
    }

    String indentStr = "  ".repeat(indent);
    out.append(indentStr);
  }

  private void writeNode(ConfigurationNode node, boolean forceInline) throws IOException {
    if (node.isList()) {
      writeArray(node, forceInline);
      return;
    }

    if (node.isMap()) {
      writeMap(node, forceInline);
      return;
    }

    Object raw = node.raw();

    if (raw instanceof String str) {
      String escaped = Toml.tomlEscape(str).toString();
      out.append('"');
      out.append(escaped);
      out.append('"');
    }

    out.append(raw.toString());
  }

  private void writeMap(ConfigurationNode node, boolean forceInline) throws IOException {
    if (forceInline) {
      out.append('{');
    }

    var map = node.childrenMap();

    if (map.isEmpty()) {
      if (forceInline) {
        out.append('}');
      }

      return;
    }

    if (forceInline) {
      indent++;
      out.newLine();
      writeIndent();
    }

    var it = map.entrySet().iterator();
    while (it.hasNext()) {
      var n = it.next();

      String key = n.getKey().toString();
      ConfigurationNode value = n.getValue();
      String escaped = Toml.tomlEscape(key).toString();

      if (!forceInline && value.isMap()) {
        if (indent > 0) {
          indent++;
          out.newLine();
          writeIndent();
        }

        out.append('[');
        out.append(optionallyQuote(escaped));
        out.append(']');

        if (indent > 0) {
          indent--;
          out.newLine();
          writeIndent();
          writeMap(value, false);
        }
      }

      out.append(optionallyQuote(escaped));
      out.append(" = ");
      writeNode(value, forceInline);

      if (forceInline && it.hasNext()) {
        out.append(", ");
      }
    }

    if (forceInline) {
      indent--;
      out.newLine();
      writeIndent();
      out.append('}');
    }
  }

  private void writeArray(ConfigurationNode node, boolean forceInline) throws IOException {
    out.append('[');

    var list = node.childrenList();

    if (list.isEmpty()) {
      out.append(']');
      return;
    }

    if (!forceInline) {
      out.newLine();
      indent++;
      writeIndent();
    }

    var it = list.listIterator();
    while (it.hasNext()) {
      ConfigurationNode n = it.next();
      writeNode(n, true);

      if (it.hasNext()) {
        out.append(',');

        if (!forceInline) {
          out.newLine();
          writeIndent();
        }
      }
    }

    if (!forceInline) {
      indent--;

      out.newLine();
      writeIndent();
    }

    out.append(']');
  }

  private String optionallyQuote(String str) {
    if (str.matches("[a-zA-Z0-9_-]*")) {
      return str;
    }

    return '"' + str + '"';
  }
}
