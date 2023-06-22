package net.forthecrown.core.grave;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.forthecrown.events.Events;
import net.forthecrown.ItemGraveService;
import net.forthecrown.BukkitServices;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GraveImpl implements ItemGraveService {

  private final Map<String, Filter> filters = new HashMap<>();

  public static void init() {
    GraveImpl impl = new GraveImpl();
    BukkitServices.register(ItemGraveService.class, impl);
    Events.register(new GraveListener());
  }

  @Override
  public Map<String, Filter> getFilters() {
    return Collections.unmodifiableMap(filters);
  }

  @Override
  public void addFilter(@NotNull String id, @NotNull Filter filter) {
    Objects.requireNonNull(filter, "Null filter");

    if (filters.containsKey(id)) {
      throw new IllegalArgumentException(
          "Filter with id '%s' has already been registered".formatted(id)
      );
    }

    filters.put(id, filter);
  }

  @Override
  public void removeFilter(@NotNull String id) {
    filters.remove(id);
  }

  @Override
  public boolean shouldRemain(@NotNull ItemStack item, @NotNull Player player) {
    if (filters.isEmpty()) {
      return false;
    }

    for (Filter f: filters.values()) {
      if (f.shouldRemain(item, player)) {
        return true;
      }
    }

    return false;
  }
}