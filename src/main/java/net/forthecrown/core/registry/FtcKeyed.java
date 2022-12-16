package net.forthecrown.core.registry;

import org.intellij.lang.annotations.Pattern;

/**
 * Something which has a key
 * @see Keys#VALID_KEY_REGEX
 * @see Keys#ensureValid(String)
 * @see Registry
 * @see Registry#register(String, Object)
 */
public interface FtcKeyed {
    /**
     * Gets the registry-applicable key of this object.
     * The returned key must adhere to the {@link Keys#VALID_KEY_REGEX}
     * regex pattern. Otherwise, it cannot be registered in a registry.
     *
     * @return The object's key
     */
    @Pattern(Keys.VALID_KEY_REGEX)
    String getKey();
}