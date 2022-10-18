package net.forthecrown.vars;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.vars.types.VarType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

@Getter
@RequiredArgsConstructor
public class VarData<T> implements ComponentLike {
    private final String name;
    private final Field field;
    private final VarType<T> type;

    private final UpdateCallback<T> updateCallback;
    private final T defaultValue;

    public @NotNull T getValue() {
        try {
            T val = (T) field.get(null);
            return val == null ? defaultValue : val;
        } catch (IllegalAccessException e) {
            return defaultValue;
        }
    }

    public void setValue(@NotNull T value) {
        try {
            field.set(null, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public void update(@NotNull T value) {
        setValue(value);

        if (updateCallback != null) {
            updateCallback.runSafe(value);
        }
    }

    public boolean isTransient() {
        return Modifier.isTransient(field.getModifiers());
    }

    @Override
    public @NotNull Component asComponent() {
        return type.display(getValue());
    }

    interface UpdateCallback<T> {
        void onUpdate(T value) throws ReflectiveOperationException;

        default void runSafe(T value) {
            try {
                onUpdate(value);
            } catch (InvocationTargetException exc) {
                throw new IllegalStateException(exc.getCause());
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}