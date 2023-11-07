package net.forthecrown.text.placeholder;

import java.util.regex.Pattern;
import net.forthecrown.Loggers;
import net.forthecrown.text.Text;
import net.forthecrown.text.UnitFormat;
import net.forthecrown.user.TimeField;
import net.forthecrown.user.User;
import net.forthecrown.user.UserService;
import net.forthecrown.user.Users;
import net.forthecrown.user.currency.Currency;
import net.forthecrown.utils.Audiences;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

public class PlayerPlaceholders implements PlaceholderSource {

  private static final Logger LOGGER = Loggers.getLogger();

  private static final Pattern USER_FIELD_PATTERN = Pattern.compile("\\.[a-zA-Z0-9_%.]+");

  private final String prefix;
  private final User user;

  public PlayerPlaceholders(String prefix, User user) {
    this.prefix = prefix;
    this.user = user;
  }

  private User getUser(Audience viewer) {
    if (user != null) {
      return user;
    }

    return Audiences.getUser(viewer);
  }

  @Override
  public TextPlaceholder getPlaceholder(String name, PlaceholderContext ctx) {
    if (!name.startsWith(prefix)) {
      return null;
    }

    var remainder = name.substring(prefix.length());
    var user = getUser(ctx.viewer());

    if (user == null) {
      return null;
    }

    if (remainder.isEmpty()) {
      return (match, render) -> user.displayName(render.viewer());
    }

    if (!USER_FIELD_PATTERN.matcher(remainder).matches()) {
      return null;
    }

    String fieldName = remainder.substring(1);
    var location = user.getLocation();

    String x     = String.format("%.2f", location.getX());
    String y     = String.format("%.2f", location.getY());
    String z     = String.format("%.2f", location.getZ());
    String yaw   = String.format("%.2f", location.getYaw());
    String pitch = String.format("%.2f", location.getPitch());

    String bx = "" + location.getBlockX();
    String by = "" + location.getBlockY();
    String bz = "" + location.getBlockZ();

    UserService service = Users.getService();

    var currencyOpt = service.getCurrencies().get(fieldName);
    if (currencyOpt.isPresent()) {
      Currency currency = currencyOpt.get();
      int value = currency.get(user.getUniqueId());
      Component formatted = currency.format(value);

      return TextPlaceholder.simple(formatted);
    }

    return switch (fieldName) {
      case "x" -> TextPlaceholder.simple(x);
      case "y" -> TextPlaceholder.simple(y);
      case "z" -> TextPlaceholder.simple(z);

      case "bx" -> TextPlaceholder.simple(bx);
      case "by" -> TextPlaceholder.simple(by);
      case "bz" -> TextPlaceholder.simple(bz);

      case "pos" -> TextPlaceholder.simple(x + " " + y + " " + z);
      case "block" -> TextPlaceholder.simple(bx + " " + by + " " + bz);

      case "world" -> TextPlaceholder.simple(Text.formatWorldName(location.getWorld()));
      case "world.id" -> TextPlaceholder.simple(location.getWorld().getName());

      case "location" -> (match, render) -> Text.prettyLocation(location, false);
      case "world_location" -> (match, render) -> Text.prettyLocation(location, true);

      case "yaw" -> TextPlaceholder.simple(yaw);
      case "pitch" -> TextPlaceholder.simple(pitch);

      case "uuid" -> TextPlaceholder.simple(user.getUniqueId());

      case "playtime" -> {
        int playtime = user.getPlayTime();
        yield TextPlaceholder.simple(UnitFormat.playTime(playtime));
      }

      case "votes" -> {
        int votes = user.getTotalVotes();
        yield TextPlaceholder.simple(UnitFormat.votes(votes));
      }

      case "property" -> (match, render) -> {
        if (match.isEmpty()) {
          return null;
        }

        return service.getUserProperties().get(match)
            .map(property -> Text.valueOf(user.get(property), render.viewer()))
            .orElse(null);
      };

      case "timestamp" -> (match, render) -> {
        if (match.isEmpty()) {
          return null;
        }

        return TimeField.REGISTRY.get(match)
            .map(user::getTime)
            .map(Text::formatDate)
            .orElse(null);
      };

      default -> null;
    };
  }
}
