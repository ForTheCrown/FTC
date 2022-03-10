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
 * The class representing a CommandVariable
 * <p>
 * A type must always be specified. You probably won't have to worry about that though, as the only place this is constructed
 * is in the {@link VarRegistry} class, where this stuff gets done for you :)
 * </p>
 * @param <T> The type stored by the variable.
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

    @Override
    public T get() {
        return value == null ? defaultValue : value;
    }

    public @Nullable T getValue() {
        return value;
    }

    public T getValue(T def) {
        return value == null ? def : value;
    }

    public Var<T> set(T value) {
        this.value = value;
        return this;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public Var<T> setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public @NotNull VarType<T> getType() {
        return type;
    }

    public Consumer<T> getUpdateListener() {
        return updateListener;
    }

    public Var<T> setUpdateListener(Consumer<T> onUpdate) {
        this.updateListener = onUpdate;
        return this;
    }

    public Var<T> update(T newValue) {
        this.value = newValue;
        if(updateListener != null) updateListener.accept(value);

        return this;
    }

    public boolean isTransient() {
        return _transient;
    }

    public void setTransient(boolean _transient) {
        this._transient = _transient;
    }

    @Override
    public String toString() {
        return type.asParsableString(value);
    }

    public String getName() {
        return name;
    }

    @Override
    public JsonElement serialize() {
        return type.serialize(value);
    }

    @Override
    public @NotNull Component asComponent() {
        return type.display(value);
    }

    public static <T> Var<T> def(String name, VarType<T> type, T defVal) {
        return VarRegistry.getSafe(name, type, defVal);
    }
}
