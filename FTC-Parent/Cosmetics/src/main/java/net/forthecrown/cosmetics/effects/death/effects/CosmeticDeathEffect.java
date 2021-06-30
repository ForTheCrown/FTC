package net.forthecrown.cosmetics.effects.death.effects;

import net.forthecrown.core.economy.CannotAffordTransactionException;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.cosmetics.custominvs.options.ClickableOption;
import net.forthecrown.cosmetics.effects.CosmeticEffect;
import net.forthecrown.cosmetics.effects.death.DeathEvent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class CosmeticDeathEffect implements CosmeticEffect {

    public static final Listener listener = new DeathEvent();

    @Override
    public int getGemCost() { return 2000; }

    @Override
    public abstract String getEffectName();

    @Override
    public abstract ItemStack getEffectItem(boolean isOwned);

    public abstract void activateEffect(Location loc);

    @Override
    public final boolean isCurrentActiveEffect(CrownUser user) {
        return user.getDeathParticle() != null && user.getDeathParticle().contains(getEffectName());
    }

    @Override
    public final boolean isOwnedBy(CrownUser user) {
        return user.getParticleDeathAvailable().contains(getEffectName());
    }

    public ClickableOption getClickableOption(CrownUser user) {
        ClickableOption option = new ClickableOption();
        // Clicking
        option.setCooldown(0);
        option.setActionOnClick(() -> {
            user.getPlayer().playSound(user.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);

            if(!user.getParticleDeathAvailable().contains(getEffectName())){
                if(user.getGems() < getGemCost()) throw new CannotAffordTransactionException(user.getPlayer());
                user.addGems(-getGemCost());

                List<String> asd = user.getParticleDeathAvailable();
                asd.add(getEffectName());
                user.setParticleDeathAvailable(asd);
            }
            user.setDeathParticle(getEffectName());
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
