package net.forthecrown.cosmetics.deaths;

import net.forthecrown.text.Text;
import net.forthecrown.cosmetics.Cosmetic;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.utils.inventory.menu.Slot;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;

public abstract class DeathEffect extends Cosmetic {

    DeathEffect(int slot, String name, Component... description) {
        super(name, Cosmetics.DEATH, Slot.of(slot), description);
    }

    DeathEffect(int slot, String name, String desc){
        this(slot, name, Text.renderString(desc));
    }

    public abstract void activate(Location loc);
}