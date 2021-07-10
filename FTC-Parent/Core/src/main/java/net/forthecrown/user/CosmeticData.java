package net.forthecrown.user;

import net.forthecrown.cosmetics.arrows.ArrowEffect;
import net.forthecrown.cosmetics.deaths.AbstractDeathEffect;

import java.util.List;

public interface CosmeticData extends UserAttachment {

    boolean hasActiveDeath();

    AbstractDeathEffect getActiveDeath();

    void setActiveDeath(AbstractDeathEffect death);

    boolean hasActiveArrow();

    ArrowEffect getActiveArrow();

    void setActiveArrow(ArrowEffect arrow);

    List<AbstractDeathEffect> getDeathEffects();

    boolean hasDeath(AbstractDeathEffect effect);

    void addDeath(AbstractDeathEffect effect);

    void removeDeath(AbstractDeathEffect effect);

    List<ArrowEffect> getArrowEffects();

    boolean hasArrow(ArrowEffect effect);

    void addArrow(ArrowEffect effect);

    void removeArrow(ArrowEffect effect);
}
