package net.forthecrown.economy.houses;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.math.MathUtil;

public class Relation implements JsonSerializable {
    public static final byte MAX_RELATION = 100;

    private byte value = 0;

    public Relation() {
    }

    public Relation(byte value) {
        setValue(value);
    }

    public byte getValue() {
        return value;
    }

    public void setValue(byte value) {
        this.value = (byte) MathUtil.clamp(value, -MAX_RELATION, MAX_RELATION);
    }

    public RelationType getType() {
        for (RelationType t: RelationType.values()) {
            if(t.inRange(value)) return t;
        }

        return null;
    }

    public void setType(RelationType type) {
        if(type.inRange(value)) return;

        setValue(type.getMin());
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(value);
    }
}
