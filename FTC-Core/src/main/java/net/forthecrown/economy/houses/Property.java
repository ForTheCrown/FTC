package net.forthecrown.economy.houses;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public abstract class Property<T> {
    public final String name;
    public final T defaultValue;

    public Property(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public abstract JsonElement serialize(T val);
    public abstract T deserialize(JsonElement element);

    public static class FloatProperty extends Property<Float> {
        public FloatProperty(String name, Float defaultValue) {
            super(name, defaultValue);
        }

        @Override
        public JsonElement serialize(Float val) {
            return new JsonPrimitive(val);
        }

        @Override
        public Float deserialize(JsonElement element) {
            return element.getAsFloat();
        }
    }
}
