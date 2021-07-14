package net.forthecrown.useables;

import net.kyori.adventure.key.Key;

/**
 * Represents a usage type's instance
 */
public interface UsageTypeInstance {

    /**
     * Gets the type as a string representation.
     * <p>
     * Used to display info about this in the list command
     * </p>
     * @return The string representation of this instance
     */
    String asString();

    /**
     * Gets the key of the type for this instance
     * @return The type's key
     */
    Key typeKey();
}
