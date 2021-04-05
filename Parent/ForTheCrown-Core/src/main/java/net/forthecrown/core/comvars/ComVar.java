package net.forthecrown.core.comvars;

import net.forthecrown.core.comvars.types.ComVarType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

//CommandVariable class
public class ComVar<T>/* implements ConfigurationSerializable */ {
    private T value;
    private final ComVarType<T> type;
    private Consumer<T> onUpdate;
    public ComVar(ComVarType<T> type, T value){
        this.value = value;
        this.type = type;
    }

    public ComVar(ComVarType<T> type){
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
