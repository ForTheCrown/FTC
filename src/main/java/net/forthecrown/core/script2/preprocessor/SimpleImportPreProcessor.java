package net.forthecrown.core.script2.preprocessor;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

class SimpleImportPreProcessor extends RegexPreProcessor {

  public SimpleImportPreProcessor() {
    super(Pattern.compile("import +['\"]([a-z0-9/.@&%$_]+)['\"];?",
        CASE_INSENSITIVE
    ));
  }

  @Override
  String replaceMatch(MatchResult result) {
    String importString = result.group(1);
    return new JsImport(importString).toString();
  }
}