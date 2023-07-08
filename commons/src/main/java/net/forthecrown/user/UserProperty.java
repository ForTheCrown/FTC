package net.forthecrown.user;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A user property is a value stored in a user's data.
 * @param <T> Property value type
 */
public interface UserProperty<T> {

  /**
   * Gets the property's default value
   * @return Default property value
   */
  @NotNull
  T getDefaultValue();

  /**
   * Gets the property's edit callback.
   * <p>
   * This callback will be called whenever the property value is edited with
   * {@link User#set(UserProperty, Object)}
   *
   * @return Edit callback, or {@code null}, if no callback is set
   */
  @Nullable
  PropertyEditCallback<T> getCallback();

  /**
   * Serializes this property's value to JSON
   * @param value value to serialize
   * @return Serialized value
   */
  @NotNull JsonElement serialize(@NotNull T value);

  /**
   * Deserializes the property's value from JSON
   * @param element JSON to deserialize from
   * @return Deserialized value
   */
  @NotNull T deserialize(@NotNull JsonElement element);

  /**
   * Property's ID number
   * <p>
   * Internally, this ID is used to index property values to allow for fast access
   *
   * @return ID number
   */
  int getId();

  /**
   * Gets the property's string ID
   * <p>
   * This string ID is used as the property's key in the user's JSON data
   *
   * @return Property string key
   */
  String getKey();

  /**
   * Callback for property value changes
   * @param <T> Property value type
   */
  interface PropertyEditCallback<T> {

    /**
     * Called when the property is updated
     *
     * @param user User the property is being updated for
     * @param value New value, will be {@link UserProperty#getDefaultValue()} if being unset
     * @param oldValue Old property value, will be {@link UserProperty#getDefaultValue()} if the
     *                 value was not set before
     */
    void onUpdate(User user, @NotNull T value, @NotNull T oldValue);
  }

  /**
   * Property builder
   * @param <T> Property value type
   */
  interface Builder<T> {

    /**
     * Sets the property's string id. This method must be called before {@link #build()}
     * @param key property key
     * @return {@code this}
     */
    Builder<T> key(String key);

    /**
     * Sets the value update callback of this property
     * @param callback New callback
     * @return {@code this}
     */
    Builder<T> callback(PropertyEditCallback<T> callback);

    /**
     * Sets the default value of the property. This method must be called before {@link #build()}
     *
     * @param value Default value
     * @return {@code this}
     */
    Builder<T> defaultValue(@NotNull T value);

    /**
     * Builds the property and registers it to the {@link UserService#getUserProperties()} registry
     * @return Created property
     * @throws NullPointerException Will be thrown if no default value is set, or if no key is set
     */
    UserProperty<T> build() throws NullPointerException;
  }
}