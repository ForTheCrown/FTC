package net.forthecrown.inventory;

import java.util.UUID;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import net.forthecrown.text.Text;
import net.forthecrown.text.placeholder.PatternedPlaceholder;
import net.forthecrown.text.placeholder.PlaceholderContext;
import net.forthecrown.text.placeholder.Placeholders;
import net.forthecrown.text.placeholder.TextPlaceholder;
import net.forthecrown.user.User;
import net.forthecrown.utils.Audiences;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public class ItemPlaceholders {

  static final TextPlaceholder SPECIAL_ITEM = new ItemPlaceholder();

  static void registerAll() {
    Placeholders.addDefault("special_item", SPECIAL_ITEM);
  }

  public static void unregister() {
    Placeholders.removeDefault("special_item");
  }
}

class ItemPlaceholder extends PatternedPlaceholder {

  static final int G_NAME = 1;
  static final int G_LEVEL = 2;

  public ItemPlaceholder() {
    super(Pattern.compile("([a-zA-Z0-9+$\\-/._]+)(?: *, *([0-9]+))?"));
  }

  @Override
  public @Nullable Component render(MatchResult result, PlaceholderContext render) {
    String name = result.group(G_NAME);
    String levelString = result.group(G_LEVEL);
    int rank;

    if (levelString == null || levelString.isEmpty()) {
      rank = 1;
    } else {
      rank = Integer.parseInt(levelString);
    }

    var opt = ExtendedItems.REGISTRY.get(name);
    if (opt.isEmpty()) {
      return null;
    }

    var type = opt.get();

    UUID viewerId;

    if (render.viewer() != null) {
      User user = Audiences.getUser(render.viewer());
      if (user == null) {
        viewerId = null;
      } else {
        viewerId = user.getUniqueId();
      }
    } else {
      viewerId = null;
    }

    var item = type.createItem(viewerId);

    for (int i = 1; i < rank; i++) {
      ExtendedItem extendedItem = type.get(item);
      extendedItem.setDisplayItem(true);

      type.rankUp(item, extendedItem);
    }

    return Text.itemDisplayName(item);
  }
}
