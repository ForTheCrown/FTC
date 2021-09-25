package net.forthecrown.comvars;

import net.forthecrown.comvars.types.ComVarType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * The class representing a CommandVariable
 * <p>
 * A type must always be specified. You probably won't have to worry about that though, as the only place this is constructed
 * is in the {@link ComVarRegistry} class, where this stuff gets done for you :)
 * </p>
 * @param <T> The type stored by the variable.
 */
public final class ComVar<T> {
    private T value;
    private final ComVarType<T> type;
    private final String name;
    private Consumer<T> onUpdate;

    ComVar(ComVarType<T> type, String name, T value){
        this.value = value;
        this.type = type;
        this.name = name;
    }

    public Component prettyDisplay(){
        return type.display(value);
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
        return type.asParsableString(value);
    }

    public String getName() {
        return name;
    }
}
