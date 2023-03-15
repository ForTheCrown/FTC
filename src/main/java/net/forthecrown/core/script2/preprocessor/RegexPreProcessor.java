package net.forthecrown.core.script2.preprocessor;

import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class RegexPreProcessor implements PreProcessor {

  private final Pattern pattern;

  public RegexPreProcessor(Pattern pattern) {
    this.pattern = Objects.requireNonNull(pattern);
  }

  @Override
  public void process(StringBuffer buffer) {
    Matcher matcher = pattern.matcher(buffer);

    while (matcher.find()) {
      var result = matcher.toMatchResult();
      String s = replaceMatch(result);

      int start = result.start();
      int end = result.end();

      buffer.replace(start, end, s);
    }
  }

  abstract String replaceMatch(MatchResult result);
}