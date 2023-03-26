package net.forthecrown.utils.stand;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Optional;
import net.forthecrown.core.Keys;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;

public class DynamicArmorStand extends AbstractDynamicStand {

  private static final NamespacedKey
      STAND_KEY = Keys.forthecrown("dynamic_stand");

  private Reference<ArmorStand> armorStand;

  public DynamicArmorStand(Location location) {
    this(location, STAND_KEY);
  }

  public DynamicArmorStand(Location location, NamespacedKey key) {
    super(location, key);
  }

  public void update(Component displayName) {
    getStand().ifPresentOrElse(stand -> {
      stand.customName(displayName);
    }, () -> {
      spawn(displayName);
    });
  }

  @Override
  public void kill() {
    if (armorStand != null && armorStand.get() != null) {
      armorStand.get().remove();
      armorStand = null;
      return;
    }

    kill(getKey());
  }

  private ArmorStand spawn(Component displayName) {
    var stand = spawn(getLocation(), getKey(), displayName);

    armorStand = new WeakReference<>(stand);
    return stand;
  }

  public Optional<ArmorStand> getStand() {
    if (armorStand != null) {
      var ref = armorStand.get();

      if (ref != null) {
        return Optional.of(ref);
      }
    }

    var nearby = getLocation().getNearbyLivingEntities(
        1.5D,
        entity -> entity.getPersistentDataContainer().has(getKey())
    );

    if (nearby.isEmpty()) {
      return Optional.empty();
    }

    var first = nearby.iterator().next();

    if (nearby.size() > 1) {
      nearby.forEach(entity -> {
        if (entity.equals(first)) {
          return;
        }

        entity.remove();
      });
    }

    return Optional.of((ArmorStand) first);
  }
}