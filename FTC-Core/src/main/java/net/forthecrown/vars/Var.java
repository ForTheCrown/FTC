package net.forthecrown.vars;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.vars.types.VarType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A class representing a single global variable var
 * @param <T> The variable's type
 */
public final class Var<T> implements JsonSerializable, Supplier<T>, ComponentLike {
    private final VarType<T> type;
    private final String name;

    private T value;
    private T defaultValue;
    private Consumer<T> updateListener;

    boolean used, _transient;

    public Var(VarType<T> type, String name, T value) {
        this.value = value;
        this.type = type;
        this.name = name;
    }

    /**
     * Gets the value of the variable. If the value == null, {@link Var#getDefaultValue()} is returned
     * @return The value of the variable
     */
    @Override
    public T get() {
        return value == null ? defaultValue : value;
    }

    /**
     * Gets the value of the var, does not perform a null check for default return values
     * @return The variable's value, null, if no value set
     */
    public @Nullable T getValue() {
        return value;
    }

    /**
     * Gets the value of this variable
     * @param def The default to return
     * @return The variable's value, or def, if the value is null
     */
    public T getValue(T def) {
        return value == null ? def : value;
    }

    /**
     * Sets the value of the variable
     * @param value The variable's value
     * @return This variable
     */
    public Var<T> set(T value) {
        this.value = value;
        return this;
    }

    /**
     * Gets the default return value for this variable
     * @return The default return variable
     */
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default return value for this variable
     * @param defaultValue The new default return value
     * @return This variable
     */
    public Var<T> setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * Gets the variable's type
     * @return The variable type
     */
    public @NotNull VarType<T> getType() {
        return type;
    }

    /**
     * Gets the update listener
     * @return The value update listener
     */
    public Consumer<T> getUpdateListener() {
        return updateListener;
    }

    /**
     * Sets the value update listener. This will be called everytime the value of this variable is updated, aka, everytime
     * {@link Var#update(Object)} is called
     * @param onUpdate The update listener
     * @return This variable
     */
    public Var<T> setUpdateListener(Consumer<T> onUpdate) {
        this.updateListener = onUpdate;
        return this;
    }

    /**
     * Updates the variable's value, will call the update listener if one is set
     * @param newValue The new value of the variable
     * @return This variable
     */
    public Var<T> update(T newValue) {
        this.value = newValue;
        if(updateListener != null) updateListener.accept(value);

        return this;
    }

    /**
     * Checks if this variable is transient
     * @return True, if this variable should not be saved, false otherwise
     */
    public boolean isTransient() {
        return _transient;
    }

    /**
     * Sets if the variable is transient, {@link Var#isTransient()}
     * @param _transient Transient status
     * @return This variable
     */
    public Var<T> setTransient(boolean _transient) {
        this._transient = _transient;
        return this;
    }

    @Override
    public String toString() {
        return type.asParsableString(value);
    }

    /**
     * Gets the variable's name
     * @return The variable's name
     */
    public String getName() {
        return name;
    }

    /**
     * Saves the variable into JSON using {@link VarType#serialize(Object)} with this variable's type and value
     * @return The serialized value of this variable
     */
    @Override
    public JsonElement serialize() {
        return type.serialize(value);
    }

    /**
     * Creates a component display of this variable
     * @return
     */
    @Override
    public @NotNull Component asComponent() {
        return type.display(value);
    }

    /**
     * Quick define method for creating vars
     * @param name The name of the var
     * @param type The var's type, most types are stored as constants in {@link  net.forthecrown.vars.types.VarTypes}
     * @param defVal The default value of the variable
     * @param <T> The Variable's type
     * @return The defined variable
     */
    public static <T> Var<T> def(String name, VarType<T> type, T defVal) {
        return VarRegistry.def(name, type, defVal);
    }
}
