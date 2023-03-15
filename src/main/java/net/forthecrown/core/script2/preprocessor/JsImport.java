package net.forthecrown.core.script2.preprocessor;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.script2.ScriptManager;
import net.forthecrown.utils.io.SerializationHelper;

class JsImport {

  static Map<String, String> placeHolders = null;
  static boolean importsAreConst = true;

  private final String path;
  private final String label;
  private final boolean classImport;

  public JsImport(String input) {
    this.path = input.trim();
    this.classImport = !path.endsWith(".js");

    String label = path;

    if (!classImport) {
      label = label.substring(0, label.length() - 3);
    }

    int dotIndex = label.lastIndexOf('.');

    if (dotIndex != -1) {
      label = label.substring(dotIndex + 1);
      Preconditions.checkState(!label.isBlank(), "Blank label in import");
    }

    this.label = label;
  }

  public JsImport(String path, String label) {
    this.path = path.trim();
    this.label = label.trim();
    this.classImport = !this.path.endsWith(".js");
  }

  public static String replacePlaceholders(String s) {
    var map = getPlaceHolders();
    String result = s;

    for (var e: map.entrySet()) {
      result = result.replaceAll("@" + e.getKey(), e.getValue());
    }

    if (result.indexOf('@') != -1) {
      Loggers.getLogger().warn(
          "Import '{}' contains unknown placeholders (result: '{}')",
          s, result
      );
    }

    return result;
  }

  private static Map<String, String> getPlaceHolders() {
    if (placeHolders == null) {
      placeHolders = new HashMap<>();

      var path = ScriptManager.getInstance()
          .getDirectory()
          .resolve("import_placeholders.toml");

      SerializationHelper.readTomlAsJson(path, wrapper -> {
        if (wrapper.has("imports_are_const")) {
          importsAreConst = wrapper.getBool("imports_are_const");
          wrapper.remove("imports_are_const");
        }

        wrapper.entrySet().forEach(entry -> {
          placeHolders.put(entry.getKey(), entry.getValue().getAsString());
        });
      });
    }

    return placeHolders;
  }

  @Override
  public String toString() {
    String path = replacePlaceholders(this.path);
    String qualifier = importsAreConst ? "const" : "var";

    if (classImport) {
      return String.format("%s %s = Java.type(\"%s\");", qualifier, label, path);
    }

    return String.format("%s %s = compile(\"%s\");", qualifier, label, path);
  }
}