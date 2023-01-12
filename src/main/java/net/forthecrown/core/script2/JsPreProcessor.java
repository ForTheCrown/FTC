package net.forthecrown.core.script2;

import it.unimi.dsi.fastutil.Pair;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;

public class JsPreProcessor {
  public static final Pattern IMPORT_PATTERN
      = Pattern.compile("(import +(['\"][@a-zA-Z0-9$_%.]+['\"](| )+(|;)+))");

  public static StringReader preprocess(Reader reader) throws IOException {
    String s = IOUtils.toString(reader);
    return new StringReader(preprocess(s));
  }

  public static String preprocess(String s) {
    s = processImports(s);
    return s;
  }

  private static String processImports(String s) {
    var matcher = IMPORT_PATTERN.matcher(s);
    StringBuilder builder = new StringBuilder();

    while (matcher.find()) {
      var str = matcher.group(1);
      var processed = replaceImport(str);
      matcher.appendReplacement(builder, processed);
    }
    matcher.appendTail(builder);

    return builder.toString();
  }

  private static String replaceImport(final String statement) {
    var s = statement.replaceAll("import", "")
        .replaceAll("['\"]", "")
        .replaceAll("@ftc", "net.forthecrown")
        .replaceAll("@bukkit", Bukkit.class.getPackageName())
        .replaceAll("@fastutil", Pair.class.getPackageName())
        .replaceAll("@jlang", Double.class.getPackageName())
        .replaceAll("@jutil", List.class.getPackageName())
        .replaceAll("@nio", ByteBuffer.class.getPackageName())
        .trim();

    if (s.endsWith(";")) {
      s = s.substring(0, s.length() - 1);
    }
    s = s.trim();

    int lastDot = s.lastIndexOf('.');

    if (lastDot == -1) {
      return statement;
    }

    var className = s.substring(lastDot + 1);

    if (className.isBlank()) {
      return statement;
    }

    return "const " + className + " = Java.type(\"" + s + "\")";
  }
}