package net.forthecrown.utils.inventory.menu.context;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ContextOption<T> {
    /** The set this optional belongs to */
    private final ContextSet parent;

    /** The ID/index of this option */
    private final int index;

    /** The option's default value */
    private final T defaultValue;

    @Override
    public int hashCode() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContextOption)) {
            return false;
        }

        ContextOption<?> option = (ContextOption<?>) o;

        return getIndex() == option.getIndex()
                && getParent() == option.getParent();
    }
}