package net.forthecrown.core.script2;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.utils.io.SerializationHelper;
import org.apache.commons.io.IOUtils;

public class JsPreProcessor {
  public static final Pattern IMPORT_PATTERN
      = Pattern.compile("(import +(['\"][@a-zA-Z0-9$_%.]+['\"](| )+(|;)+))");

  static Map<String, String> placeHolders = null;

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
        .trim();

    s = replacePlaceholders(s);

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

  private static String replacePlaceholders(final String importStatement) {
    var map = getOrLoadPlaceholders();
    String result = importStatement;

    for (var e: map.entrySet()) {
      result = result.replaceAll("@" + e.getKey(), e.getValue());
    }

    if (result.indexOf('@') != -1) {
      Loggers.getLogger().warn(
          "Import '{}' contains unknown placeholders (result: '{}')",
          importStatement, result
      );
    }

    return result;
  }

  private static Map<String, String> getOrLoadPlaceholders() {
    if (placeHolders == null) {
      placeHolders = new HashMap<>();

      var path = ScriptManager.getInstance()
          .getDirectory()
          .resolve("import_placeholders.toml");

      SerializationHelper.readTomlAsJson(path, wrapper -> {
        wrapper.entrySet().forEach(entry -> {
          placeHolders.put(entry.getKey(), entry.getValue().getAsString());
        });
      });
    }

    return placeHolders;
  }
}