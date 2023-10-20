package net.forthecrown.scripts.preprocessor;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import net.forthecrown.scripts.module.ImportInfo.BindingImport;

class SimpleImportProcessor extends RegexProcessor {

  static final int GROUP_PATH = 2;
  static final int GROUP_STAR = 1;

  public SimpleImportProcessor() {
    super(Pattern.compile("import(\\s+\\*\\s+from)?\\s+['\"]([^'\"]+)['\"];?"));
  }

  @Override
  protected void process(
      SubWriter writer,
      MatchResult result,
      List<PreProcessorCallback> callbacks
  ) {
    String importName = PreProcessor.replacePlaceholders(result.group(GROUP_PATH));
    writer.deleteInput();

    String starGroup = result.group(GROUP_STAR);
    boolean starImport = starGroup != null && starGroup.contains("*");

    List<BindingImport> bindings = starImport ? List.of(BindingImport.STAR) : List.of();

    JsImport jsImport = new JsImport(false, importName, null, bindings);
    callbacks.add(jsImport);
  }
}
