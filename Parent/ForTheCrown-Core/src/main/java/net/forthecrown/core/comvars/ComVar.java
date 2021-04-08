package net.forthecrown.core.comvars;

import net.forthecrown.core.comvars.types.ComVarType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * The class representing a CommandVariable
 * <p>
 * A type must always be specified. You probably won't have to worry about that though, as the only place this is constructed
 * is in the {@link ComVars} class, where this stuff gets done for you :)
 * </p>
 * @param <T> The type stored by the variable, can be an int, String or whatever you want it to be
 */
public class ComVar<T>/* implements ConfigurationSerializable */ {
    private T value;
    private final ComVarType<T> type;
    private Consumer<T> onUpdate;

    ComVar(ComVarType<T> type, T value){
        this.value = value;
        this.type = type;
    }
    ComVar(ComVarType<T> type){
        this.type = type;
    }

    public @Nullable T getValue() {
        return value;
    }

    public T getValue(T def){
        return value == null ? def : value;
    }

    public ComVar<T> setValue(T value) {
        this.value = value;
        return this;
    }

    public @NotNull ComVarType<T> getType() {
        return type;
    }

    public Consumer<T> getOnUpdate() {
        return onUpdate;
    }

    public ComVar<T> setOnUpdate(Consumer<T> onUpdate) {
        this.onUpdate = onUpdate;
        return this;
    }

    public ComVar<T> update(T newValue){
        this.value = newValue;
        if(onUpdate != null) onUpdate.accept(value);
        return this;
    }

    @Override
    public String toString() {
        return type.asString(value);
    }

    /*@Override
    public @NotNull Map<String, Object> serialize() {
        return null;
    }*/
}
