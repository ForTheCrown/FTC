package net.forthecrown.cosmetics.arrows;

import org.bukkit.Particle;

public final class ArrowEffects {
    private ArrowEffects() {}

    public static final ArrowEffect
            FLAME           = new ArrowEffect(10, Particle.FLAME, "Flame", "Works perfectly with flame arrows."),
            SNOWY           = new ArrowEffect(11, Particle.SNOWBALL, "Snowy", "To stay in the Christmas spirit."),
            SNEEZE          = new ArrowEffect(12, Particle.SNEEZE, "Sneeze", "Cover the place in juicy snot."),
            CUPIDS_ARROWS   = new ArrowEffect(13, Particle.HEART, "Cupid's Arrows", "Time to do some matchmaking..."),
            CUPIDS_TWIN     = new ArrowEffect(14, Particle.DAMAGE_INDICATOR, "Cupid's Evil Twin", "Time to undo some matchmaking..."),
            STICKY_TRAIL    = new ArrowEffect(15, Particle.DRIPPING_HONEY, "Sticky Trail", "For those who enjoy looking at the trail lol"),
            SMOKE           = new ArrowEffect(16, Particle.CAMPFIRE_COSY_SMOKE, "Smoke", "Pretend to be a cannon."),
            SOULS           = new ArrowEffect(19, Particle.SOUL, "Souls", "Scary souls escaping from your arrows"),
            FIREWORK        = new ArrowEffect(20, Particle.FIREWORKS_SPARK, "Firework", "Almost as if you're using a crossbow");
}