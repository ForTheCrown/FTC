package net.forthecrown.user.property;

import com.google.gson.JsonElement;
import lombok.Getter;
import net.forthecrown.user.User;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

/**
 * A single property a user can have
 * <p>
 * Each implementation of this class has to
 * specify a {@link #serialize(Object)} and
 * {@link #deserialize(JsonElement)} method
 * for saving and loading the value type stored
 * by this property.
 * <p>
 * Note: Each property is automatically added
 * to {@link Properties#USER_PROPERTIES} when
 * its constructed, thus be careful of when you
 * instantiate property instances.
 *
 * @param <T> The property's type
 *
 * @see PropertyMap To see how properties are stored
 *                  and serialized on a user-by-user basis
 * @see Properties For all the current properties stored
 *                 as constants
 */
@Getter
public abstract class UserProperty<T> {
    /**
     * The property's name
     */
    @NotNull
    private final String key;

    /**
     * The default value of the property,
     * must not be null
     */
    @NotNull
    private final T defaultValue;

    /**
     * The index of the property.
     * <p>
     * This is created automatically when the property
     * is created and is generated by {@link net.forthecrown.core.registry.Registry},
     * more specifically the {@link Properties#USER_PROPERTIES} registry
     */
    private final int index;

    public UserProperty(@NotNull String name, @NotNull T defaultValue) {
        this.key = name;
        this.defaultValue = Validate.notNull(defaultValue, "Property '%s' had null defaultValue", name);

        var holder = Properties.USER_PROPERTIES.register(name, this);
        this.index = holder.getId();
    }

    /**
     * Deserializes the given element
     * @param element The element to deserialize from
     * @return The deserialized object
     */
    public abstract T deserialize(JsonElement element);

    /**
     * Serializes the given object to JSON
     * @param t The object to serialize
     * @return The JSON representation of that object
     */
    public abstract JsonElement serialize(T t);

    /**
     * Called whenever this property's value is updated.
     * Only called when the given user is online
     * @param user The user whose value was updated for this property.
     */
    public void onUpdate(User user) {
    }
}