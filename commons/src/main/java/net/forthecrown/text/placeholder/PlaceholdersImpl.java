package net.forthecrown.text.placeholder;

import static net.kyori.adventure.text.Component.text;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.jetbrains.annotations.Nullable;

class PlaceholdersImpl implements PlaceholderList {

  private static final Pattern PATTERN
      = Pattern.compile("\\$\\{([a-zA-Z0-9_$.]+)(?:: *((?:\\\\}|[^}])+))?\\}");

  static final Map<String, TextPlaceholder> defaultPlaceholders = new HashMap<>();

  private final Map<String, TextPlaceholder> placeholders = new HashMap<>();
  private boolean useDefaults = false;

  @Override
  public Component render(Component base) {
    return render(base, null);
  }

  @Override
  public Component render(Component base, @Nullable Audience viewer) {
    TextReplacementConfig config = TextReplacementConfig.builder()
        .match(PATTERN)
        .replacement((result, builder) ->  renderPlaceholder(result, viewer))
        .build();

    return base.replaceText(config);
  }

  private Component renderPlaceholder(MatchResult result, @Nullable Audience viewer) {
    String placeholderName = result.group(1);

    String input = result.group(2);
    if (input == null) {
      input = "";
    } else {
      input.replace("\\}", "}");
    }

    TextPlaceholder placeholder = getPlaceholder(placeholderName);

    if (placeholder == null) {
      return text(result.group());
    }

    Component rendered = placeholder.render(input, viewer);

    if (rendered == null) {
      return text(result.group());
    }

    return rendered;
  }

  @Override
  public PlaceholderList add(String name, TextPlaceholder placeholder) {
    Objects.requireNonNull(name, "Null name");
    Objects.requireNonNull(placeholder, "Null placeholder");

    placeholders.put(name, placeholder);
    return this;
  }

  @Override
  public PlaceholderList add(String name, Object value) {
    return add(name, TextPlaceholder.simple(value));
  }

  @Override
  public PlaceholderList add(String name, Supplier<?> supplier) {
    return add(name, TextPlaceholder.simple(supplier));
  }

  @Override
  public PlaceholderList useDefaults() {
    useDefaults = true;
    return this;
  }

  @Override
  public boolean usesDefaults() {
    return useDefaults;
  }

  private TextPlaceholder getPlaceholder(String name) {
    TextPlaceholder placeholder = placeholders.get(name);
    if (placeholder != null) {
      return placeholder;
    }

    if (!useDefaults) {
      return null;
    }

    return defaultPlaceholders.get(name);
  }

  @Override
  public PlaceholderList clear() {
    placeholders.clear();
    return this;
  }

  @Override
  public Map<String, TextPlaceholder> getPlaceholders() {
    return Collections.unmodifiableMap(placeholders);
  }
}
