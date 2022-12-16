package net.forthecrown.utils.inventory.menu.context;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * A set of {@link ContextOption}s that apply to a certain
 * inventory.
 */
@NoArgsConstructor(staticName = "create")
public class ContextSet {
    private final Set<ContextOption> options = new ObjectOpenHashSet<>();

    /**
     * Creates a new option with a null default value
     * <p>
     * Delegate method for {@link #newOption(Object)} with
     * null parameter
     * @param <T> The option's type
     * @return The created option
     * @see #newOption(Object)
     */
    public <T> @NotNull ContextOption<T> newOption() {
        return newOption(null);
    }

    /**
     * Creates a new option with the given default value
     * @param defaultValue The option's default value
     * @param <T> The option's type
     * @return The created option
     */
    public <T> @NotNull ContextOption<T> newOption(@Nullable T defaultValue) {
        ContextOption<T> option = new ContextOption<>(this, options.size(), defaultValue);
        options.add(option);

        return option;
    }

    public boolean has(ContextOption option) {
        return options.contains(option);
    }

    /**
     * Creates an inventory context of this set's options. All
     * options within the created context will have each option's
     * default values.
     *
     * @return The created context
     */
    public InventoryContext createContext() {
        var values = new Object[options.size()];

        for (var f: options) {
            values[f.getIndex()] = f.getDefaultValue();
        }

        return new InventoryContext(this, values);
    }
}