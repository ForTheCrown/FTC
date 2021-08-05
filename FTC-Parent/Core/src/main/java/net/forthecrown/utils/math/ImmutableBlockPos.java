package net.forthecrown.utils.math;

import com.google.gson.JsonObject;
import net.forthecrown.serializer.JsonSerializable;

/**
 * An immutable block pos
 */
public interface ImmutableBlockPos extends JsonSerializable, Cloneable {
    int getX();
    int getY();
    int getZ();

    ImmutableBlockPos clone();

    @Override
    default JsonObject serialize() {
        JsonObject json = new JsonObject();

        json.addProperty("x", getX());
        json.addProperty("y", getY());
        json.addProperty("z", getZ());

        return json;
    }
}
