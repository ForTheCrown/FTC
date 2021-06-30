package net.forthecrown.cosmetics.effects.arrow.effects;

import net.forthecrown.core.user.CrownUser;
import net.forthecrown.cosmetics.custominvs.options.ClickableOption;
import net.forthecrown.cosmetics.effects.CosmeticEffect;
import net.forthecrown.cosmetics.effects.arrow.ArrowEvent;
import org.bukkit.Particle;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public abstract class CosmeticArrowEffect implements CosmeticEffect {

    public static final Listener listener = new ArrowEvent();

    @Override
    public String getEffectName() {
        return getParticle().name();
    }

    @Override
    public abstract ItemStack getEffectItem();

    public abstract double getParticleSpeed();

    public abstract Particle getParticle();

    @Override
    public final boolean isCurrentActiveEffect(CrownUser user) {
        return user.getArrowParticle() != null && user.getArrowParticle().name().contains(getEffectName());
    }

    @Override
    public final boolean isOwnedBy(CrownUser user) {
        return user.getParticleArrowAvailable().contains(getEffectName());
    }

    public ClickableOption getClickableOption(CrownUser user) {
        ClickableOption option = new ClickableOption();
        // Clicking
        option.setCooldown(0);
        option.setActionOnClick(() -> {
            // TODO: Buy effect or set it as active
            System.out.println("Clicked on + " + getEffectName());
        });

        // Item to display
        ItemStack item = getEffectItem();
        if (isOwnedBy(user)) setItemOwned(item);
        if (isCurrentActiveEffect(user)) addGlow(item);
        option.setItem(item);

        return option;
    }
}
