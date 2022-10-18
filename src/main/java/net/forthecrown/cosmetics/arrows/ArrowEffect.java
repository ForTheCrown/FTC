package net.forthecrown.cosmetics.arrows;

import net.forthecrown.text.Text;
import net.forthecrown.cosmetics.Cosmetic;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.utils.inventory.menu.Slot;
import net.kyori.adventure.text.Component;
import org.bukkit.Particle;

public class ArrowEffect extends Cosmetic {

    private final Particle particle;

    ArrowEffect(int slot, Particle particle, String name, Component... description) {
        super(name, Cosmetics.ARROWS, Slot.of(slot), description);
        this.particle = particle;
    }

    ArrowEffect(int slot, Particle particle, String name, String desc){
        this(slot, particle, name, Text.renderString(desc));
    }

    public Particle getParticle() {
        return particle;
    }
}