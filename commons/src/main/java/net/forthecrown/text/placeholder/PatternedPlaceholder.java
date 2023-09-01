package net.forthecrown.text.placeholder;

import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public abstract class PatternedPlaceholder implements TextPlaceholder {

  private final Pattern pattern;

  public PatternedPlaceholder(Pattern pattern) {
    Objects.requireNonNull(pattern);
    this.pattern = pattern;
  }

  @Override
  public @Nullable Component render(String match, PlaceholderContext render) {
    Matcher matcher = pattern.matcher(match);

    if (!matcher.matches()) {
      return null;
    }

    MatchResult result = matcher.toMatchResult();
    return render(result, render);
  }

  public abstract @Nullable Component render(MatchResult result, PlaceholderContext render);
}
