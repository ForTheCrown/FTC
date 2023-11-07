package net.forthecrown.text;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
class TextSplitter extends AbstractFlattenerListener {

  private final Pattern pattern;

  private final List<Component> result = new ObjectArrayList<>();
  private TextComponent.Builder current = Component.text();
  private boolean builderEmpty = true;

  public List<Component> split(Component input) {
    result.clear();
    styles.clear();
    style = Style.empty();
    current = Component.text();

    Text.FLATTENER.flatten(input, this);

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

    var matcher = pattern.matcher(text);

    var results = matcher.results().toList();
    var it = results.listIterator();

    if (!it.hasNext()) {
      pushToCurrent(text);
      return;
    }

    while (it.hasNext()) {
      var result = it.next();

      if (result.start() == 0) {
        pushToResult();
        continue;
      }

      int lastEnd;
      it.previous();

      if (it.hasPrevious()) {
        lastEnd = it.previous().end();
        it.next();
      } else {
        lastEnd = 0;
      }

      it.next();

      String string = text.substring(lastEnd, result.start());
      pushToCurrent(string);
      pushToResult();
    }

    var last = it.previous();
    if (last.end() == text.length()) {
      return;
    }

    var untilEnd = text.substring(last.end());
    pushToCurrent(untilEnd);
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