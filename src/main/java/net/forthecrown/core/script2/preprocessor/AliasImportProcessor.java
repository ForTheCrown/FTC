package net.forthecrown.core.script2.preprocessor;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

class AliasImportProcessor extends RegexPreProcessor {

  public AliasImportProcessor() {
    super(Pattern.compile(
        "import *\\* *as +([a-z_%&$][a-z0-9_%&$]+) +from +['\"]([a-z0-9/.@ $&_%]+)['\"];?",
        CASE_INSENSITIVE
    ));
  }

  @Override
  String replaceMatch(MatchResult result) {
    String alias = result.group(1);
    String path = result.group(2);
    return new JsImport(path, alias).toString();
  }
}