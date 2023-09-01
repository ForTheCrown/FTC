package net.forthecrown.core.placeholder;

import static net.kyori.adventure.text.Component.text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import net.forthecrown.Loggers;
import net.forthecrown.text.placeholder.PlaceholderContext;
import net.forthecrown.text.placeholder.PlaceholderList;
import net.forthecrown.text.placeholder.PlaceholderRenderer;
import net.forthecrown.text.placeholder.PlaceholderSource;
import net.forthecrown.text.placeholder.TextPlaceholder;
import net.forthecrown.utils.context.Context;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class PlaceholderRendererImpl implements PlaceholderRenderer {

  private static final Logger LOGGER = Loggers.getLogger();

  private final PlaceholderServiceImpl service;
  private final List<PlaceholderSource> sources;
  private final PlaceholderListImpl selfList;

  public PlaceholderRendererImpl(PlaceholderServiceImpl service) {
    this.service = service;
    this.sources = new ArrayList<>();
    this.selfList = service.newList();

    addSource(selfList);
  }

  @Override
  public Component render(Component base, @Nullable Audience viewer, Context ctx) {
    if (service.getPlugin().getFtcConfig().placeholdersDisabled()) {
      return base;
    }

    PlaceholderContext render = new PlaceholderContext(viewer, this, ctx);

    TextReplacementConfig config = TextReplacementConfig.builder()
        .match(PATTERN)
        .replacement((result, builder) -> renderPlaceholder(result, render))
        .build();

    return base.replaceText(config);
  }

  private Component renderPlaceholder(MatchResult result, PlaceholderContext render) {
    String placeholderName = result.group(1);

    String input = result.group(2);
    if (input == null) {
      input = "";
    } else {
      input = input.replace("\\}", "}").trim();
    }

    TextPlaceholder placeholder = getPlaceholder(placeholderName, render);

    if (placeholder == null) {
      LOGGER.debug("Unknown placeholder named '{}', full input '{}'",
          placeholderName, result.group()
      );

      return text(result.group());
    }

    Component rendered = placeholder.render(input, render);

    if (rendered == null) {
      return text(result.group());
    }

    return rendered;
  }

  @Override
  public TextPlaceholder getPlaceholder(String name, PlaceholderContext ctx) {
    for (PlaceholderSource source : sources) {
      var placeholder = source.getPlaceholder(name, ctx);

      if (placeholder == null) {
        continue;
      }

      return placeholder;
    }

    return null;
  }

  @Override
  public PlaceholderRenderer useDefaults() {
    sources.addAll(service.getDefaultSources());
    return this;
  }

  @Override
  public PlaceholderRenderer addSource(PlaceholderSource source) {
    sources.add(source);
    return this;
  }

  @Override
  public PlaceholderList getPlaceholderList() {
    return selfList;
  }
}
