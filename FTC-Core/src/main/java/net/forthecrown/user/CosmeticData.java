package net.forthecrown.user;

import net.forthecrown.cosmetics.CosmeticEffect;
import net.forthecrown.cosmetics.arrows.ArrowEffect;
import net.forthecrown.cosmetics.deaths.DeathEffect;
import net.forthecrown.cosmetics.travel.TravelEffect;

import java.util.Set;

/**
 * Represents the user's cosmetic data
 */
public interface CosmeticData extends UserAttachment {

    default boolean hasEffect(CosmeticEffect effect) {
        return (effect instanceof DeathEffect && hasDeath((DeathEffect) effect)) ||
                (effect instanceof ArrowEffect && hasArrow((ArrowEffect) effect)) ||
                (effect instanceof TravelEffect && hasTravel((TravelEffect) effect));
    }

    //----- Death Effects -----

    /**
     * Checks if the user has an active death effect
     * @return Whether the user has a death effect selected
     */
    default boolean hasActiveDeath() {
        return getActiveDeath() != null;
    }

    /**
     * Gets the user's current death effect
     * @return The user's death effect, null, if none selected
     */
    DeathEffect getActiveDeath();

    /**
     * Sets the user's active death effect
     * @param death The death effect, can be null, to unselect
     */
    void setActiveDeath(DeathEffect death);

    /**
     * Gets all the death effects available to this user
     * @return All the user's death effects
     */
    Set<DeathEffect> getDeathEffects();

    /**
     * Checks if the user has the given death effect.
     * @param effect The effect to check for
     * @return Whether the user has the given effect
     */
    boolean hasDeath(DeathEffect effect);

    /**
     * Gives the user the given effect
     * @param effect The effect to add
     */
    void addDeath(DeathEffect effect);

    /**
     * Removes the given effect from the user
     * @param effect The effect to remove
     */
    void removeDeath(DeathEffect effect);

    //----- Arrow Effects -----

    /**
     * Checks whether the user has an active arrow effect
     * @return Whether the user has an active arrow effect
     */
    default boolean hasActiveArrow() {
        return getActiveArrow() != null;
    }

    /**
     * Gets the user's active arrow effect
     * @return The user's active arrow effect, null, if none selected
     */
    ArrowEffect getActiveArrow();

    /**
     * Sets the user's active arrow effect
     * @param arrow The arrow effect, can be null, to unselect
     */
    void setActiveArrow(ArrowEffect arrow);

    /**
     * Gets all the arrow effects available to this user
     * @return All the user's arrow effects
     */
    Set<ArrowEffect> getArrowEffects();

    /**
     * Checks if the user has the given arrow effect.
     * @param effect The effect to check for
     * @return Whether the user has the given effect
     */
    boolean hasArrow(ArrowEffect effect);

    /**
     * Gives the user the given effect
     * @param effect The effect to add
     */
    void addArrow(ArrowEffect effect);

    /**
     * Removes the given effect from the user
     * @param effect The effect to remove
     */
    void removeArrow(ArrowEffect effect);

    //----- Travel Effects -----

    /**
     * Checks whether the user has an active travel effect
     * @return Whether the user has an active travel effect
     */
    default boolean hasActiveTravel() {
        return getActiveTravel() != null;
    }

    /**
     * Gets the user's active travel effect
     * @return The user's active travel effect, null, if none selected
     */
    TravelEffect getActiveTravel();

    /**
     * Sets the user's active travel effect
     * @param effect The travel effect, can be null, to unselect
     */
    void setActiveTravel(TravelEffect effect);

    /**
     * Gets all the travel effects available to this user
     * @return All the user's arrow effects
     */
    Set<TravelEffect> getTravelEffects();

    /**
     * Checks if the user has the given travel effect.
     * @param effect The effect to check for
     * @return Whether the user has the given effect
     */
    boolean hasTravel(TravelEffect effect);

    /**
     * Gives the user the given effect
     * @param effect The effect to add
     */
    void addTravel(TravelEffect effect);

    /**
     * Removes the given effect from the user
     * @param effect The effect to remove
     */
    void removeTravel(TravelEffect effect);
}
