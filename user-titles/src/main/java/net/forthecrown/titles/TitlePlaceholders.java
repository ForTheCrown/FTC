package net.forthecrown.titles;

import net.forthecrown.text.placeholder.Placeholders;
import net.forthecrown.text.placeholder.TextPlaceholder;

public class TitlePlaceholders {

  static final String TITLE_PLACEHOLDER = "rank";
  static final TextPlaceholder RANK = (match, render) -> {
    if (match.isEmpty()) {
      return null;
    }

    var rankOpt = UserRanks.REGISTRY.get(match);
    return rankOpt.map(UserRank::getTruncatedPrefix).orElse(null);

  };

  static void registerAll() {
    Placeholders.addDefault(TITLE_PLACEHOLDER, RANK);
  }

  static void unregister() {
    Placeholders.removeDefault(TITLE_PLACEHOLDER);
  }
}
