/**
 * The Registry package contains several classes for the use of 'registry' objects.
 * Registries are how we easily store data with a common 'key', this key being a
 * namespace key, eg: 'minecraft:stone'.
 * <p></p>
 * We use registries so we have a common way of serializing data without needing
 * to serialize an entire object, but rather a key which points to an object in
 * a registry.
 * <p></p>
 * Some registries need to be sealed off after the plugin startup has finished as
 * we can't afford any changes to said registry, this may be for various reasons,
 * Cosmetic registries get closed off due to the cosmetic inventory, which is
 * created during initialization, to remove or add any elements into it after that
 * would be bad.
 * To accomplish this registry closing, we use a {@link net.forthecrown.registry.CloseableRegistry}
 * <p></p>
 * Most registries are stored as contants in {@link net.forthecrown.registry.Registries}
 */
package net.forthecrown.registry;