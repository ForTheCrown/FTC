package net.forthecrown.cosmetics.effects.arrow.effects;

import net.forthecrown.cosmetics.effects.CosmeticEffect;
import net.forthecrown.cosmetics.effects.arrow.ArrowEvent;
import net.forthecrown.inventory.custom.options.ClickableOption;
import net.forthecrown.user.CrownUser;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class CosmeticArrowEffect implements CosmeticEffect {

    public static final Listener listener = new ArrowEvent();

    @Override
    public int getGemCost() { return 1000; }

    @Override
    public String getEffectName() {
        return getParticle().name();
    }

    @Override
    public abstract ItemStack getEffectItem(boolean isOwned);

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
            user.getPlayer().playSound(user.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);

            if(!user.getParticleArrowAvailable().contains(getParticle())){
                if(user.getGems() < getGemCost()) //throw new CannotAffordTransactionException(user.getPlayer());
                user.addGems(-getGemCost());

                List<Particle> set = user.getParticleArrowAvailable();
                set.add(getParticle());
                user.setParticleArrowAvailable(set);
            }
            user.setArrowParticle(getParticle());
            // TODO: update view
        });

        // Item to display
        ItemStack item = getEffectItem(isOwnedBy(user));
        if (isOwnedBy(user)) setItemOwned(item);
        if (isCurrentActiveEffect(user)) addGlow(item);
        option.setItem(item);

        return option;
    }
}
