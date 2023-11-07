package net.forthecrown.cosmetics;

import static net.forthecrown.cosmetics.Cosmetic.create;

import net.forthecrown.registry.Registry;
import org.bukkit.Particle;

public class ArrowCosmetics {

  public static final Cosmetic<Particle> FLAME
      = create(Particle.FLAME, 10, "Flame", "Works perfectly with flame arrows.");

  public static final Cosmetic<Particle> SNOWY
      = create(Particle.SNOWBALL, 11, "Snowy", "To stay in the Christmas spirit.");

  public static final Cosmetic<Particle> SNEEZE
      = create(Particle.SNEEZE, 12, "Sneeze", "Cover the place in juicy snot.");

  public static final Cosmetic<Particle> CUPIDS_ARROWS
      = create(Particle.HEART, 13, "Cupid's Arrows", "Time to do some matchmaking...");

  public static final Cosmetic<Particle> CUPIDS_TWIN
      = create(Particle.DAMAGE_INDICATOR, 14, "Cupid's Evil Twin", "Time to undo some matchmaking...");

  public static final Cosmetic<Particle> STICKY_TRAIL
      = create(Particle.DRIPPING_HONEY, 15, "Sticky Trail", "For those who enjoy looking at the trail lol");

  public static final Cosmetic<Particle> SMOKE
      = create(Particle.CAMPFIRE_COSY_SMOKE, 16, "Smoke", "Pretend to be a cannon.");

  public static final Cosmetic<Particle> SOULS
      = create(Particle.SOUL, 19, "Souls", "Scary souls escaping from your arrows");

  public static final Cosmetic<Particle> FIREWORK
      = create(Particle.FIREWORKS_SPARK, 20, "Firework", "Almost as if you're using a crossbow");

  static void registerAll(Registry<Cosmetic<Particle>> r) {
    r.register("flame",             FLAME);
    r.register("snowy",             SNOWY);
    r.register("sneeze",            SNEEZE);
    r.register("cupids_arrows",     CUPIDS_ARROWS);
    r.register("cupids_evil_twin",  CUPIDS_TWIN);
    r.register("sticky_trail",      STICKY_TRAIL);
    r.register("smoke",             SMOKE);
    r.register("souls",             SOULS);
    r.register("firework",          FIREWORK);
  }
}