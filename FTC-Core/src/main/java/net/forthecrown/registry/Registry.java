 package net.forthecrown.registry;

 import com.google.gson.JsonElement;
 import net.forthecrown.core.Keys;
 import net.forthecrown.utils.JsonUtils;
 import net.kyori.adventure.key.Keyed;
 import net.minecraft.nbt.Tag;

 /**
 * Represents a map like object which can store object with a {@link net.kyori.adventure.key.Key} as the, well, key.
 * @param <T>
 */
public interface Registry<T> extends FtcRegistry<T, T>, Iterable<T>, Keyed {
    /**
     * Gets the size of the registry
     * @return the size of the registry
     */
    int size();

    /**
     * Clears the registry
     */
    void clear();

    /**
     * Checks whether the registry is empty
     * @return Whether the registry is empty or not
     */
    boolean isEmpty();

    default T read(JsonElement element) {
        return get(JsonUtils.readKey(element));
    }

    default T read(Tag tag) {
        return get(Keys.parse(tag.getAsString()));
    }
}
