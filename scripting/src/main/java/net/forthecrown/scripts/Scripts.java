package net.forthecrown.scripts;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.Objects;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.utils.io.Results;
import net.forthecrown.utils.io.TagOps;
import net.forthecrown.utils.io.source.Source;
import net.forthecrown.utils.io.source.Sources;

public final class Scripts {
  private Scripts() {}

  public static final Codec<Script> CODEC = createScriptCodec(false);
  public static final Codec<Script> DIRECT_JS_CODEC = createScriptCodec(true);

  public static final Codec<Script> LOADING_DIRECT_CODEC = DIRECT_JS_CODEC.comapFlatMap(
      script -> {
        try {
          script.compile();
        } catch (ScriptLoadException exc) {
          return Results.error(exc.getMessage());
        }

        return Results.success(script);
      },
      script -> script
  );

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
    return loadScriptSource0(new Dynamic<>(JsonOps.INSTANCE, element), assumeRawJs);
  }

  public static Source loadScriptSource(BinaryTag tag, boolean assumeRawJs) {
    return loadScriptSource0(new Dynamic<>(TagOps.OPS, tag), assumeRawJs);
  }

  public static <S> DataResult<Source> loadScriptSource(Dynamic<S> dynamic, boolean assumeRawJs) {
    try {
      return DataResult.success(loadScriptSource0(dynamic, assumeRawJs));
    } catch (IllegalArgumentException exc) {
      return DataResult.error(exc::getMessage);
    }
  }

  private static <S> Source loadScriptSource0(Dynamic<S> dynamic, boolean assumeRawJs) {
    var path = getService().getScriptsDirectory();
    return Sources.load(dynamic, path, assumeRawJs);
  }

  public static Script loadScript(JsonElement element, boolean assumeRawJs) {
    return newScript(loadScriptSource(element, assumeRawJs));
  }

  public static Script fromScriptFile(String filePath) {
    return newScript(scriptFileSource(filePath));
  }

  private static Codec<Script> createScriptCodec(boolean assumeRaw) {
    return new Codec<>() {
      @Override
      public <T> DataResult<Pair<Script, T>> decode(DynamicOps<T> ops, T input) {
        return loadScriptSource(new Dynamic<>(ops, input), assumeRaw)
            .map(Scripts::newScript)
            .map(script -> Pair.of(script, input));
      }

      @Override
      public <T> DataResult<T> encode(Script input, DynamicOps<T> ops, T prefix) {
        return input.getSource().save(ops);
      }
    };
  }
}