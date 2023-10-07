package net.forthecrown.placeholderapi;

import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.manager.LocalExpansionManager;
import net.forthecrown.text.placeholder.PlaceholderContext;
import net.forthecrown.text.placeholder.PlaceholderSource;
import net.forthecrown.text.placeholder.TextPlaceholder;

public class FtcPlaceholderSource implements PlaceholderSource {

  private final LocalExpansionManager manager;

  public FtcPlaceholderSource(LocalExpansionManager manager) {
    this.manager = manager;
  }

  private ObjectIntPair<PlaceholderExpansion> findExpansion(String name) {
    int index = name.indexOf('_');

    if (index == -1) {
      return makePair(name, index);
    }

    while (true) {
      String substring = name.substring(0, index);
      PlaceholderExpansion exp = manager.getExpansion(substring);

      if (exp != null) {
        return ObjectIntPair.of(exp, index);
      }

      index = name.substring(index + 1).indexOf(' ');

      if (index == -1) {
        return makePair(name, -1);
      }
    }
  }

  private ObjectIntPair<PlaceholderExpansion> makePair(String name, int index) {
    var exp = manager.getExpansion(name);
    if (exp == null) {
      return null;
    }
    return ObjectIntPair.of(exp, index);
  }

  @Override
  public TextPlaceholder getPlaceholder(String name, PlaceholderContext ctx) {
    var pair = findExpansion(name);

    if (pair == null) {
      return null;
    }

    String params;

    if (pair.rightInt() == -1) {
      params = "";
    } else {
      params = name.substring(pair.rightInt() + 1);
    }

    return new ApiPlaceholder(pair.left(), params);
  }
}
