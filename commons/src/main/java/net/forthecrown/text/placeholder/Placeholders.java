package net.forthecrown.text.placeholder;

import java.util.UUID;
import net.forthecrown.BukkitServices;
import net.forthecrown.text.PlayerMessage;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * Placeholder-related utility class
 */
public final class Placeholders {
  private Placeholders() {}

  private static PlaceholderService service;

  public static PlaceholderService getService() {
    return service == null
        ? (service = BukkitServices.loadOrThrow(PlaceholderService.class))
        : service;
  }

  public static PlaceholderList newList() {
    return getService().newList();
  }

  public static PlaceholderRenderer newRenderer() {
    return getService().newRenderer();
  }

  public static Component renderString(String str, Audience viewer) {
    var message = PlayerMessage.allFlags(str).create(viewer);
    var renderer = newRenderer().useDefaults();
    return renderer.render(message, viewer);
  }

  public static void addDefault(String name, TextPlaceholder placeholder) {
    getService().getDefaults().add(name, placeholder);
  }

  public static void removeDefault(String name) {
    getService().getDefaults().remove(name);
  }

  public static void createPlayerPlaceholders(PlaceholderRenderer list, String prefix, Player player) {
    createPlayerPlaceholders(list, prefix, Users.get(player));
  }

  public static void createPlayerPlaceholders(PlaceholderRenderer list, String prefix, UUID playerId) {
    var service = Users.getService();
    var entry = service.getLookup().getEntry(playerId);

    if (entry == null) {
      return;
    }

    createPlayerPlaceholders(list, prefix, Users.get(entry));
  }

  public static void createPlayerPlaceholders(PlaceholderRenderer list, String prefix, User player) {
    PlayerPlaceholders placeholders = new PlayerPlaceholders(prefix, player);
    list.addSource(placeholders);
  }
}
