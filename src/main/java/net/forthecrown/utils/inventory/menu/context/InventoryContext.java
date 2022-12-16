package net.forthecrown.utils.inventory.menu.context;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class InventoryContext {
    /** A context that has no options */
    public static final InventoryContext EMPTY = new InventoryContext(null, null) {
        @Override
        public <T> boolean has(@NotNull ContextOption<T> option) {
            return false;
        }

        @Override
        public <T> T get(@NotNull ContextOption<T> option) {
            return option.getDefaultValue();
        }

        @Override
        public <T> void set(@NotNull ContextOption<T> option, T value) {
        }
    };

    /** The set that created this context */
    @Getter
    private final ContextSet contextSet;

    /** The option value array, {@link ContextOption#getIndex()} is used to get the value */
    private final Object[] options;

    /**
     * Gets an option's value
     * @param option The option to get the value of
     * @param <T> The option's type
     * @return The gotten value
     */
    public <T> @Nullable T get(@NotNull ContextOption<T> option) {
        validateOption(option);
        return (T) options[option.getIndex()];
    }

    public <T> @NotNull T getOrThrow(@NotNull ContextOption<T> option) {
        return Optional.ofNullable(get(option)).orElseThrow();
    }

    public <T> void set(@NotNull ContextOption<T> option, @Nullable T value) {
        validateOption(option);
        options[option.getIndex()] = value;
    }

    private void validateOption(ContextOption option) {
        Validate.isTrue(contextSet.has(option), "Invalid option, not contained in parent set");
    }

    public <T> boolean has(@NotNull ContextOption<T> option) {
        return contextSet.has(option);
    }
}