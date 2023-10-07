package net.forthecrown.scripts.pack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.ListCodec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.forthecrown.scripts.pack.PackExport.Export;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.Results;

public class PackLoader {

  public static final Pattern EXPORT_PATTERN
      = Pattern.compile("([a-zA-Z0-9_$]+)(?: +as +([a-zA-Z0-9_$]+))");

  private static final int GROUP_NAME = 1;
  private static final int GROUP_ALIAS = 2;

  public static final Codec<Export> EXPORT_CODEC = Codec.STRING.comapFlatMap(string -> {
    Matcher matcher = EXPORT_PATTERN.matcher(string);

    if (!matcher.matches()) {
      return Results.error("Invalid export '%s'", string);
    }

    String name = matcher.group(GROUP_NAME);
    String alias = matcher.group(GROUP_ALIAS);

    return Results.success(new Export(name, alias));
  }, Export::toString);

  public static final Decoder<List<Export>> EXPORT_LIST_DECODER = new ListCodec<>(EXPORT_CODEC);

  static DataResult<PackMeta> load(JsonElement element, LoadContext context) {
    if (element == null || element.isJsonNull()) {
      return Results.error("Null value");
    }

    if (!element.isJsonObject()) {
      return Results.error("Not an object");
    }

    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    if (!json.has("main-script")) {
      return Results.error("No main set");
    }

    DataResult<Path> main = getScriptFile(json.get("main-script"), context.directory())
        .mapError(string -> "Couldn't load main-script: " + string);

    if (main.error().isPresent()) {
      return Results.cast(main);
    }

    PackMeta meta = new PackMeta();
    meta.setMainScript(main.getOrThrow(false, null));
    meta.setDirectory(context.directory());

    if (json.has("exports")) {
      DataResult<List<PackExport>> exports = loadExports(json.get("exports"), context.directory());

      return exports.map(packExports -> {
        meta.getExports().addAll(packExports);
        return meta;
      });
    }

    return Results.success(meta);
  }

  static DataResult<List<PackExport>> loadExports(JsonElement element, Path directory) {
    if (element == null || element.isJsonNull()) {
      return Results.error("Null value");
    }

    if (!element.isJsonObject()) {
      return Results.error("Not an object");
    }

    var obj = element.getAsJsonObject();
    DataResult<List<PackExport>> results = DataResult.success(new ArrayList<>());

    for (Entry<String, JsonElement> entry : obj.entrySet()) {
      results.apply2(List::add, loadExport(entry.getValue(), directory, entry.getKey()));
    }

    return results;
  }

  static DataResult<PackExport> loadExport(JsonElement element, Path directory, String name) {
    if (element == null || element.isJsonNull()) {
      return Results.error("Null value");
    }

    if (element.isJsonPrimitive()) {
      return getScriptFile(element, directory).map(path -> new PackExport(name, path, List.of()));
    }

    if (!element.isJsonObject()) {
      return Results.error("Not a script-file name nor an object");
    }

    JsonObject obj = element.getAsJsonObject();

    if (!obj.has("script")) {
      return Results.error("No 'scripts' value set");
    }

    DataResult<Path> pathRes = getScriptFile(obj.get("script"), directory);
    DataResult<List<Export>> exports;

    if (obj.has("exports")) {
      exports = EXPORT_LIST_DECODER.parse(JsonOps.INSTANCE, obj.get("exports"));
    } else {
      exports = Results.success(List.of());
    }

    return pathRes.apply2((path, exports1) -> new PackExport(name, path, exports1), exports);
  }

  static DataResult<Path> getScriptFile(JsonElement element, Path directory) {
    return Codec.STRING.parse(JsonOps.INSTANCE, element)
        .flatMap(string -> {
          String fileName = string + (string.endsWith(".js") ? "" : ".js");
          Path path = directory.resolve(fileName);

          if (!Files.exists(path)) {
            return Results.error("No script file named '%s' exists", string);
          }

          return Results.success(path);
        });
  }
}
