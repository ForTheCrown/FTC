package net.forthecrown;

import static net.forthecrown.BukkitServices.loadOrThrow;

final class ServiceInstances {

  static Cooldowns cooldown;
  static InventoryStorage inventoryStorage;
  static ItemGraveService grave;

  public static Cooldowns getCooldown() {
    return cooldown == null
        ? (cooldown = loadOrThrow(Cooldowns.class))
        : cooldown;
  }

  public static InventoryStorage getInventoryStorage() {
    return inventoryStorage == null
        ? (inventoryStorage = loadOrThrow(InventoryStorage.class))
        : inventoryStorage;
  }

  public static ItemGraveService getGrave() {
    return grave == null
        ? (grave = loadOrThrow(ItemGraveService.class))
        : grave;
  }
}