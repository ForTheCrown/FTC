package net.forthecrown.utils.math;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;

/**
 * An immutable block pos
 */
public interface ImmutableVector3i extends JsonSerializable, Cloneable {
    int getX();
    int getY();
    int getZ();

    ImmutableVector3i clone();

    @Override
    default JsonElement serialize() {
        // Serialize X Y and Z cords as an integer array, takes up less space,
        // I think lol
        return JsonUtils.writeIntArray(getX(), getY(), getZ());
    }
}
