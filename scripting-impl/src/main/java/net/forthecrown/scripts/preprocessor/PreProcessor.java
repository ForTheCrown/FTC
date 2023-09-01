package net.forthecrown.scripts.preprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.scripts.Script;
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
  private final List<PreProcessorCallback> callbacks = new ArrayList<>();

  public PreProcessor(StringBuffer source) {
    this.source = source;
  }

  static String replacePlaceholders(String jsImport) {
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

  public void runCallbacks(Script script) {
    for (int i = 0; i < callbacks.size(); i++) {
      PreProcessorCallback callback = callbacks.get(i);
      callback.postProcess(script);
    }
  }
}

