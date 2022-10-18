package net.forthecrown.user;

import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

/**
 * A user component is an object attached to a user that can be serialized
 * and deserialized.
 * <p>
 * Please see {@link Components} for further info on how user components
 * function.
 */
@RequiredArgsConstructor
public abstract class UserComponent {
    /**
     * The user this component belongs to
     */
    @Getter
    protected final User user;

    /**
     * The component's type
     * @see Components
     */
    @Getter
    private final ComponentType type;

    /**
     * Loads the component from the given JSON element
     * @param element The element to load from
     */
    public abstract void deserialize(@Nullable JsonElement element);

    /**
     * Serializes the component
     * @return The JSON representation of this component's data
     */
    public abstract @Nullable JsonElement serialize();
}