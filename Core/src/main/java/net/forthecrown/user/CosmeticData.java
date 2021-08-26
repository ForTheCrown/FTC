package net.forthecrown.user;

import net.forthecrown.cosmetics.arrows.ArrowEffect;
import net.forthecrown.cosmetics.deaths.DeathEffect;
import net.forthecrown.cosmetics.travel.TravelEffect;

import java.util.Set;

/**
 * Represents the user's cosmetic data
 */
public interface CosmeticData extends UserAttachment {

    default boolean hasActiveDeath() {
        return getActiveDeath() != null;
    }

    DeathEffect getActiveDeath();

    void setActiveDeath(DeathEffect death);


    default boolean hasActiveArrow() {
        return getActiveArrow() != null;
    }

    ArrowEffect getActiveArrow();

    void setActiveArrow(ArrowEffect arrow);


    default boolean hasActiveTravel() {
        return getActiveTravel() != null;
    }

    TravelEffect getActiveTravel();

    void setActiveTravel(TravelEffect effect);


    Set<DeathEffect> getDeathEffects();

    boolean hasDeath(DeathEffect effect);

    void addDeath(DeathEffect effect);

    void removeDeath(DeathEffect effect);


    Set<ArrowEffect> getArrowEffects();

    boolean hasArrow(ArrowEffect effect);

    void addArrow(ArrowEffect effect);

    void removeArrow(ArrowEffect effect);


    Set<TravelEffect> getTravelEffects();

    boolean hasTravel(TravelEffect effect);

    void addTravel(TravelEffect effect);

    void removeTravel(TravelEffect effect);
}
