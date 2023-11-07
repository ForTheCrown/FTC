package net.forthecrown.guilds.unlockables;

import lombok.experimental.UtilityClass;
import net.forthecrown.guilds.unlockables.nameformat.UnlockableBrackets;
import net.forthecrown.guilds.unlockables.nameformat.UnlockableColorType;
import net.forthecrown.guilds.unlockables.nameformat.UnlockableStyle;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;

public @UtilityClass class Unlockables {

  public final Registry<Unlockable> REGISTRY = Registries.newFreezable();

  public static void registerAll() {
    UnlockableColor.registerAll(REGISTRY);

    registerAll(UnlockableChunkUpgrade.values());
    registerAll(UnlockableRankSlot.values());
    registerAll(Upgradable.values());
    registerAll(UnlockableSetting.values());
    registerAll(UnlockableBrackets.values());
    registerAll(UnlockableColorType.values());
    registerAll(UnlockableStyle.values());

    // Role color doesn't have to be registered
    // because it's not serialized
    registerAll(DiscordUnlocks.ROLE);
    registerAll(DiscordUnlocks.CHANNEL);
  }

  private static void registerAll(Unlockable... unlockables) {
    for (var u : unlockables) {
      String key;

      if (REGISTRY.contains(u.getKey())) {
        key = u.getClass().getSimpleName() + "/" + u.getKey();
      } else {
        key = u.getKey();
      }

      REGISTRY.register(key, u);
    }
  }
}