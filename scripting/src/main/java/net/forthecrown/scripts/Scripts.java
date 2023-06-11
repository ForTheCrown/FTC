package net.forthecrown.scripts;

import com.google.gson.JsonElement;
import java.nio.file.Path;
import java.util.Objects;
import net.forthecrown.utils.io.source.Source;
import net.forthecrown.utils.io.source.Sources;

public final class Scripts {
  private Scripts() {}

  private static ScriptService service;

  public static ScriptService getService() {
    return Objects.requireNonNull(service, "Service not created yet");
  }

  static void setService(ScriptService service) {
    Scripts.service = service;
  }

  public static Script newScript(Source source) {
    return getService().newScript(source);
  }

  public static Source scriptFileSource(String filePath) {
    Path scriptsDir = getService().getScriptsDirectory();
    Path file = scriptsDir.resolve(filePath);
    return Sources.fromPath(file, scriptsDir);
  }

  public static Source loadScriptSource(JsonElement element, boolean assumeRawJs) {
    var path = getService().getScriptsDirectory();
    return Sources.loadFromJson(element, path, assumeRawJs);
  }

  public static Script loadScript(JsonElement element, boolean assumeRawJs) {
    return newScript(loadScriptSource(element, assumeRawJs));
  }

  public static Script fromScriptFile(String filePath) {
    return newScript(scriptFileSource(filePath));
  }
}