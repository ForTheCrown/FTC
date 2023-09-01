package net.forthecrown.scripts.pack;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.Results;

public class PackLoader {

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

    return Results.success(meta);
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
