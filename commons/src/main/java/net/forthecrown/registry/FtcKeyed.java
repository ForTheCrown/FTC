package net.forthecrown.registry;

import org.intellij.lang.annotations.Pattern;

/**
 * Something which has a key
 *
 * @see Registries#VALID_KEY_REGEX
 * @see Registries#ensureValidKey(String)
 * @see Registry
 * @see Registry#register(String, Object)
 */
public interface FtcKeyed {

  /**
   * Gets the registry-applicable key of this object. The returned key must adhere to the
   * {@link Registries#VALID_KEY_REGEX} regex pattern. Otherwise, it cannot be registered in a registry.
   *
   * @return The object's key
   */
  @Pattern(Registries.VALID_KEY_REGEX)
  String getKey();
}