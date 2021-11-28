package net.forthecrown.cosmetics.arrows;

import net.forthecrown.core.Crown;
import net.forthecrown.cosmetics.CosmeticConstants;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.registry.Registries;
import net.kyori.adventure.text.Component;
import org.bukkit.Particle;

public final class ArrowEffects {
    private ArrowEffects() {}

    private static BuiltInventory INVENTORY;

    public static final ArrowEffect
            FLAME           = register(10, Particle.FLAME, "Flame", "Works perfectly with flame arrows."),
            SNOWY           = register(11, Particle.SNOWBALL, "Snowy", "To stay in the Christmas spirit."),
            SNEEZE          = register(12, Particle.SNEEZE, "Sneeze", "Cover the place in juicy snot."),
            CUPIDS_ARROWS   = register(13, Particle.HEART, "Cupid's Arrows", "Time to do some matchmaking..."),
            CUPIDS_TWIN     = register(14, Particle.DAMAGE_INDICATOR, "Cupid's Evil Twin", "Time to undo some matchmaking..."),
            STICKY_TRAIL    = register(15, Particle.DRIPPING_HONEY, "Sticky Trail", "For those who enjoy looking at the trail lol"),
            SMOKE           = register(16, Particle.CAMPFIRE_COSY_SMOKE, "Smoke", "Pretend to be a cannon."),
            SOULS           = register(19, Particle.SOUL, "Souls", "Scary souls escaping from your arrows"),
            FIREWORK        = register(20, Particle.FIREWORKS_SPARK, "Firework", "Almost as if you're using a crossbow");

    public static void init(){
        INVENTORY = CosmeticConstants.baseInventory(36, Component.text("Arrow Effects"), true)
                .addAll(Registries.ARROW_EFFECTS)
                .add(CosmeticConstants.NO_ARROW)
                .build();

        Registries.ARROW_EFFECTS.close();
        Crown.logger().info("Arrow Effects registered");
    }

    private static ArrowEffect register(int slot, Particle particle, String name, String desc){
        return register(new ArrowEffect(slot, particle, name, desc));
    }

    private static ArrowEffect register(ArrowEffect effect){
        return Registries.ARROW_EFFECTS.register(effect.key(), effect);
    }

    public static BuiltInventory getInventory(){
        return INVENTORY;
    }
}
