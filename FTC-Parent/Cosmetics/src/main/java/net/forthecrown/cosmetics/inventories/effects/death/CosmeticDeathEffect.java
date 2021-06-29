package net.forthecrown.cosmetics.inventories.effects.death;

import net.forthecrown.core.user.CrownUser;
import net.forthecrown.cosmetics.inventories.effects.CosmeticEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public abstract class CosmeticDeathEffect implements CosmeticEffect {

    public static final Listener listener = new DeathEvent();

    @Override
    public abstract String getEffectName();

    @Override
    public abstract ItemStack getEffectItem();

    public abstract void activateEffect(Location loc);


    @Override
    public final boolean isCurrentActiveEffect(CrownUser user) {
        return user.getParticleDeathAvailable().contains(getEffectName());
    }

    @Override
    public final boolean isOwnedBy(CrownUser user) {
        return user.getDeathParticle() != null && user.getDeathParticle().contains(getEffectName());
    }

    public void setItemOwned(ItemStack item) { item.setType(Material.GRAY_DYE); }

}
