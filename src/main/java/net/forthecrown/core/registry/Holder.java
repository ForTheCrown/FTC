package net.forthecrown.core.registry;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

/**
 * A single entry within a Registry.
 * <p>
 * Holder's are immutable, because once registered, they do not change unless
 * they are unregistered.
 * <p>
 * Also, I will not lie, the reason this is called a 'Holder' instead of
 * something like 'RegistryEntry' is because I copied the vanilla registries
 * naming convention, which was named Holder for a reason, while this is not,
 * this is just an immutable entry. Also, 'Holder' is a lot less characters lol
 *
 * @see Registry
 * @param <V> The entry's type
 */
@Data
public final class Holder<V> {
    /** The holder's key */
    private final @NotNull String key;

    /**
     * The ID of the holder, acts as the index of the holder's index in the
     * type lookup array
     */
    private final int id;

    /** The holder's value */
    @EqualsAndHashCode.Exclude
    private final @NotNull V value;
}