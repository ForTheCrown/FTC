package net.forthecrown.text.parse;

import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import lombok.Getter;
import net.kyori.adventure.text.Component;

public abstract class TextFunction {

  @Getter
  private final Pattern pattern;

  @Getter
  private final Pattern escapablePattern;

  public TextFunction(Pattern pattern) {
    Objects.requireNonNull(pattern);
    this.pattern = pattern;

    String patternValue = pattern.pattern();

    if (patternValue.startsWith("\\\\?")) {
      this.escapablePattern = pattern;
    } else {
      this.escapablePattern = Pattern.compile("\\\\?" + patternValue);
    }
  }

  public boolean test(TextContext context) {
    return true;
  }

  public abstract Component format(MatchResult result, TextContext context);
}