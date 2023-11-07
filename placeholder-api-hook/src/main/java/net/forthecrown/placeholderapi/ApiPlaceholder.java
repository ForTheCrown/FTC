package net.forthecrown.placeholderapi;

import com.google.common.base.Strings;
import me.clip.placeholderapi.PlaceholderHook;
import net.forthecrown.Loggers;
import net.forthecrown.text.placeholder.PlaceholderContext;
import net.forthecrown.text.placeholder.TextPlaceholder;
import net.forthecrown.user.User;
import net.forthecrown.utils.Audiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public record ApiPlaceholder(PlaceholderHook hook, String params) implements TextPlaceholder {

  private static final Logger LOGGER = Loggers.getLogger();

  @Override
  public @Nullable Component render(String match, PlaceholderContext render) {
    OfflinePlayer player;

    if (render.viewer() == null) {
      player = null;
    } else {
      User user = Audiences.getUser(render.viewer());
      player = user == null ? null : user.getOfflinePlayer();
    }

    String combinedParams;

    if (Strings.isNullOrEmpty(params)) {
      combinedParams = match;
    } else if (Strings.isNullOrEmpty(match)) {
      combinedParams = params;
    } else  {
      combinedParams = params + ":" + match;
    }

    var text = hook.onRequest(player, combinedParams);

    if (text == null) {
      return null;
    }

    return LegacyComponentSerializer.legacySection().deserialize(text);
  }
}
