package net.forthecrown.scripts.preprocessor;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import net.forthecrown.scripts.module.ImportInfo.BindingImport;

class SelectiveImportProcessor extends RegexProcessor {

  static final int GROUP_PATH = 3;
  static final int GROUP_ALIAS = 2;
  static final int GROUP_BINDINGS = 1;

  public SelectiveImportProcessor() {
    super(Pattern.compile(
        "import\\s+\\{([a-zA-Z0-9_$, \\s]+)}(?:\\s+as\\s+([a-zA-Z0-9_$]+))?\\s+from\\s+\"([^\"]+)\";?"
    ));
  }

  @Override
  protected void process(
      SubWriter writer,
      MatchResult result,
      List<PreProcessorCallback> callbacks
  ) {
    String path = PreProcessor.replacePlaceholders(result.group(GROUP_PATH));
    String rawValues = result.group(GROUP_BINDINGS);
    String alias = result.group(GROUP_ALIAS);

    String[] values = rawValues.trim().split("\\s*,+\\s*");
    var list = toBindings(result.group(), values);

    writer.deleteInput();

    JsImport jsImport = new JsImport(!Strings.isNullOrEmpty(alias), path, alias, list);
    callbacks.add(jsImport);
  }

  List<BindingImport> toBindings(String fullImport, String[] values) {
    List<BindingImport> imports = new ArrayList<>(values.length);

    for (String s : values) {
      BindingImport bind;

      if (s.contains(" as ")) {
        String[] split = s.split(" +as +");
        Preconditions.checkState(split.length == 2, "Invalid import: '%s'", fullImport);
        bind = new BindingImport(split[0], split[1]);
      } else {
        bind = new BindingImport(s, s);
      }

      imports.add(bind);
    }

    return imports;
  }
}
