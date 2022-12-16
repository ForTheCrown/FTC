package net.forthecrown.cosmetics.deaths;

import net.kyori.adventure.text.Component;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;

public class EnderRingDeathEffect extends DeathEffect {
    EnderRingDeathEffect() {
        super(14, "Ender Ring",
                Component.text("Ender particles doing ring stuff. "),
                Component.text("Makes you scream like an Enderman.")
        );
    }

    @Override
    public void activate(Location loc) {
        loc.getWorld().playSound(loc, Sound.ENTITY_ILLUSIONER_PREPARE_BLINDNESS, 1.5f, 1f);
        double y2 = loc.getY();

        for (int i = 0; i < 3; i++) {
            loc.setY(y2+i);

            for (int j = 0; j < 5; j++) {
                loc.getWorld().playEffect(loc, Effect.ENDER_SIGNAL, 1);
            }
        }
    }
}