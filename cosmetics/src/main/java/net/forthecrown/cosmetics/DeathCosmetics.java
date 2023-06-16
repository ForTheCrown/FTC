package net.forthecrown.cosmetics;

import static net.forthecrown.cosmetics.Cosmetic.create;

import net.forthecrown.registry.Registry;

public class DeathCosmetics {

  public static Cosmetic<DeathEffect> ENDER_RING = create(
      DeathEffect.ENDER_RING,
      14,
      "Ender Ring",
      "Ender particles doing ring stuff. ",
      "Makes you scream like an Enderman."
  );

  public static Cosmetic<DeathEffect> EXPLOSION = create(
      DeathEffect.EXPLOSION,
      15,
      "Creeper",
      "Always wanted to know what that feels like..."
  );

  public static Cosmetic<DeathEffect> SOUL_DEATH = create(
      DeathEffect.SOUL_DEATH,
      11,
      "Souls",
      "Scary souls escaping your body"
  );

  public static Cosmetic<DeathEffect> TOTEM = create(
      DeathEffect.TOTEM,
      12,
      "Faulty Totem",
      "The particles are there, but you still die?"
  );

  static void registerAll(Registry<Cosmetic<DeathEffect>> r) {
    r.register("ender_ring",    ENDER_RING);
    r.register("creeper",       EXPLOSION);
    r.register("souls",         SOUL_DEATH);
    r.register("faulty_totem",  TOTEM);
  }
}