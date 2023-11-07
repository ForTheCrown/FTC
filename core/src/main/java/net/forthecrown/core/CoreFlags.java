package net.forthecrown.core;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public final class CoreFlags {

  public static final StateFlag TRAPDOOR_USE = new StateFlag("trapdoor-use", true);
  public static final StateFlag WILD_ALLOWED = new StateFlag("wild-allowed", false);

  static void registerAll() {
    FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
    registry.register(TRAPDOOR_USE);
    registry.register(WILD_ALLOWED);
  }
}
