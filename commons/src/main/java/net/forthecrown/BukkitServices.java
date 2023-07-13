package net.forthecrown;

import static org.bukkit.Bukkit.getServicesManager;

import java.util.Optional;
import net.forthecrown.utils.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

public final class BukkitServices {
  private BukkitServices() {}

  public static <T> Optional<T> load(Class<T> type) {
    return Optional.ofNullable(getServicesManager().load(type));
  }

  public static <T> T loadOrThrow(Class<T> type) {
    return load(type).orElseThrow(() -> {
      throw new IllegalStateException("Missing implementation for service: " + type);
    });
  }

  public static <T> void register(Class<T> type, T value) {
    Plugin plugin = PluginUtil.getCallingPlugin();
    register(type, value, plugin);
  }

  public static <T> void register(Class<T> type, T value, Plugin plugin) {
    getServicesManager().register(type, value, plugin, ServicePriority.Normal);
  }
}