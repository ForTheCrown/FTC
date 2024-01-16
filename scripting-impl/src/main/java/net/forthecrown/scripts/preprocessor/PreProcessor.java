package net.forthecrown.scripts.preprocessor;

import com.mojang.datafixers.util.Unit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.scripts.Script;
import net.forthecrown.utils.Result;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class PreProcessor {

  static final Processor[] PROCESSORS = {
      new SimpleImportProcessor(),
      new ImportFromProcessor(),
      new SelectiveImportProcessor()
  };

  @Getter @Setter
  static Map<String, String> importPlaceholders;

  private final StringBuffer source;

  @Getter
  private final List<PreProcessorCallback> callbacks = new ArrayList<>();

  public PreProcessor(StringBuffer source) {
    this.source = source;
  }

  public static String replacePlaceholders(String jsImport) {
    if (importPlaceholders == null || importPlaceholders.isEmpty()) {
      return jsImport;
    }

    String result = jsImport;
    for (var e: importPlaceholders.entrySet()) {
      result = result.replace("@" + e.getKey(), e.getValue());
    }

    return result;
  }

  public String run() {
    for (Processor processor : PROCESSORS) {
      processor.run(source, callbacks);
    }

    return source.toString();
  }

  public Result<Unit> runCallbacks(Script script) {
    Result<Unit> result = Result.success(Unit.INSTANCE);

    for (int i = 0; i < callbacks.size(); i++) {
      PreProcessorCallback callback = callbacks.get(i);
      var callbackResult = callback.postProcess(script);

      result = result.combine(
          callbackResult,
          (string, string2) -> string + "\n" + string2,
          (unit, unit2) -> unit
      );
    }

    return result;
  }
}

