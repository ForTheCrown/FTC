package net.forthecrown.user;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import net.forthecrown.user.UserLookup.LookupEntry;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public final class Users {
  private Users() {}

  private static UserService service;

  public static UserService getService() {
    return Objects.requireNonNull(service,
        "Service not created (Error during initialization or calling too early?)"
    );
  }

  public static void setService(UserService service) {
    if (Users.service != null) {
      throw new IllegalStateException("Tried changing already set UserService");
    }

    Users.service = service;
  }

  public static User get(OfflinePlayer player) {
    return get(player.getUniqueId());
  }

  public static User get(UUID uuid) {
    var lookup = getService().getLookup();
    var entry = lookup.getEntry(uuid);
    return get(entry);
  }

  public static User get(String str) {
    var lookup = getService().getLookup();
    var entry = lookup.query(str);
    return get(entry);
  }

  public static User get(LookupEntry entry) {
    return getService().getUser(entry);
  }

  public static void forEachUser(Consumer<User> consumer) {
    Bukkit.getOnlinePlayers().forEach(player -> {
      User user = get(player);
      consumer.accept(user);
    });
  }

  public static Collection<User> getOnline() {
    return service.getOnlineUsers();
  }
}