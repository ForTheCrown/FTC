package net.forthecrown.utils.text;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
class TextSplitter extends AbstractFlattenerListener {

  public static final ComponentFlattener FLATTENER = ComponentFlattener.basic()
      .toBuilder()
      .complexMapper(TranslatableComponent.class, (component, consumer) -> {
        consumer.accept(
            GlobalTranslator.render(component, Locale.ENGLISH)
        );
      })
      .unknownMapper(component -> {
        throw Util.newException("Don't know how to split: %s", component);
      })
      .build();

  private final Pattern pattern;

  private final List<Component> result = new ObjectArrayList<>();
  private TextComponent.Builder current = Component.text();
  private boolean builderEmpty = true;

  public List<Component> split(Component input) {
    result.clear();
    styles.clear();
    style = Style.empty();
    current = Component.text();

    FLATTENER.flatten(input, this);

    if (!builderEmpty) {
      pushToResult();
    }

    return new ObjectArrayList<>(result);
  }

  @Override
  public void component(@NotNull String text) {
    if (text.isEmpty()) {
      return;
    }

    if (pattern.matcher(text).matches()) {
      pushToResult();
      return;
    }

    String[] split = pattern.split(text, -1);

    if (split.length == 1) {
      pushToCurrent(text);
      return;
    }

    for (int i = 0; i < split.length; i++) {
      String s = split[i];

      if (s.isEmpty()) {
        pushToResult();
        continue;
      }

      pushToCurrent(s);

      // If not last
      if (i != split.length - 1) {
        pushToResult();
      }
    }
  }

  private void pushToCurrent(String text) {
    current.append(Component.text(text, style));
    builderEmpty = text.isEmpty();
  }

  private void pushToResult() {
    result.add(current.build());
    current = Component.text();
    builderEmpty = true;
  }
}