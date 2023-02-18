package net.forthecrown.core.script2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface ScriptSource {
  Reader openReader() throws IOException;

  String getName();

  static ScriptSource of(Path path) {
    return new FileSource(path);
  }

  static ScriptSource of(String s) {
    return new RawSource(s);
  }

  static ScriptSource readSource(JsonElement element, boolean assumeRawJs) {
    if (element instanceof JsonObject obj) {
      if (obj.has("js")) {
        return of(obj.get("js").getAsString());
      }

      if (obj.has("path")) {
        return of(
            ScriptManager.getInstance()
                .getScriptFile(obj.get("path").getAsString())
        );
      }

      throw new IllegalStateException(
          "Object did not have either 'path' or 'js' values: " + obj
      );
    }

    var str = element.getAsString();

    if (assumeRawJs) {
      return of(str);
    } else {
      return of(
          ScriptManager.getInstance().getScriptFile(str)
      );
    }
  }

  @RequiredArgsConstructor
  class RawSource implements ScriptSource {
    private final String js;

    @Override
    public Reader openReader() throws IOException {
      return new StringReader(js);
    }

    @Override
    public String toString() {
      return getName();
    }

    @Override
    public String getName() {
      return "<eval>";
    }
  }

  @Getter
  class FileSource implements ScriptSource {
    private final Path path;
    private final String name;

    public FileSource(Path path) {
      this.path = path;
      this.name = deriveName(path);
    }

    /**
     * Derives the script's name from the given path.
     * <p>
     * If the script is in the script directory, then it's relative path inside the script directory
     * is returned, otherwise, the script's entire path is returned in string form
     */
    private static String deriveName(Path path) {
      var str = path.toString();
      var scriptDirectory = ScriptManager.getInstance()
          .getDirectory();

      if (str.contains(scriptDirectory.toString())) {
        return scriptDirectory.relativize(path).toString();
      }

      return path.toString();
    }

    @Override
    public Reader openReader() throws IOException {
      return Files.newBufferedReader(path);
    }

    @Override
    public String toString() {
      return name;
    }
  }
}