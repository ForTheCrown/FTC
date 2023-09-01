package net.forthecrown.scripts.preprocessor;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import net.forthecrown.scripts.module.ImportInfo.BindingImport;

class ImportFromProcessor extends RegexProcessor {

  static final int GROUP_PATH = 3;
  static final int GROUP_ALIAS = 2;
  static final int GROUP_STAR = 1;

  public ImportFromProcessor() {
    super(Pattern.compile(
        "import( +\\* +as)? +([a-zA-Z_$][a-zA-Z0-9_$]*) +from +['\"]([^'\"]+)['\"];?"
    ));
  }

  @Override
  protected void process(
      SubWriter writer,
      MatchResult result,
      List<PreProcessorCallback> callbacks
  ) {
    String alias = result.group(GROUP_ALIAS);
    String path = PreProcessor.replacePlaceholders(result.group(GROUP_PATH));

    String starGroup = result.group(GROUP_STAR);
    boolean star = starGroup != null && starGroup.contains("*");

    List<BindingImport> bindings = star ? List.of(BindingImport.STAR) : List.of();

    writer.deleteInput();

    JsImport jsImport = new JsImport(true, path, alias, bindings);
    callbacks.add(jsImport);
  }
}
