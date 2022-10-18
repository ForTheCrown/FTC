package net.forthecrown.vars;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.vars.types.VarType;
import net.forthecrown.vars.types.VarTypes;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Objects;

public class VarFactory {

    public Collection<VarData> getVariables(Class<?> c) throws IllegalAccessException {
        ObjectList<VarData> result = new ObjectArrayList<>();

        Var classVar = c.getDeclaredAnnotation(Var.class);

        if (classVar != null) {
            Validate.isTrue(classVar.callback().isBlank(),
                    "Cannot specify callback for entire class"
            );

            Validate.isTrue(classVar.type().isBlank(),
                    "Cannot specify var type for entire class"
            );
        }

        for (var f: c.getDeclaredFields()) {
            Var fieldVar = f.getDeclaredAnnotation(Var.class);
            AnnotationData d = AnnotationData.combine(classVar, fieldVar);

            if (classVar != null) {
                int mods = f.getModifiers();

                if (Modifier.isFinal(mods) || !Modifier.isStatic(mods)) {
                    continue;
                }

                result.add(fromField(d, f));
                continue;
            }

            if (fieldVar == null) {
                continue;
            }

            result.add(fromField(d, f));
        }

        return result;
    }

    public <T> VarData<T> fromField(AnnotationData var, Field field) throws IllegalAccessException {
        Validate.isTrue(!Modifier.isFinal(field.getModifiers()),
                "Field '%s' was final, cannot use final fields for vars",
                field.getName()
        );

        Validate.isTrue(Modifier.isStatic(field.getModifiers()),
                "Field '%s' was not static, cannot use non-static fields for vars",
                field.getName()
        );

        VarType<T> type;

        if (var.type().isBlank()) {
            type = VarTypes.BY_TYPE.get(field.getType());

            Validate.notNull(type, "Unknown type for var: '%s'", field.getType().getName());
        } else {
            type = VarTypes.TYPE_REGISTRY
                    .get(var.type())
                    .orElseThrow(() -> exception("Unknown type key: '%s'", var.type()));
        }

        field.setAccessible(true);

        return new VarData<>(
                var.prefix() + field.getName(),
                field,
                type,
                getCallback(var, field),
                (T) Objects.requireNonNull(field.get(null), "Field had null default value, this is not allowed")
        );
    }

    private <T> VarData.UpdateCallback<T> getCallback(AnnotationData var, Field field) {
        if (var.callback().isBlank()) {
            return null;
        }

        Class declaring = field.getDeclaringClass();
        var callback = var.callback();

        try {
            Method callbackMethod = getMethod(declaring, callback, field.getType());

            return value -> {
                callbackMethod.invoke(null, value);
            };
        } catch (NoSuchMethodException exc) {
            try {
                Method difCallback = getMethod(declaring, callback);
                return value -> difCallback.invoke(null);
            } catch (NoSuchMethodException e) {
                throw exception("Could not get callback method for var: '%s', no such method",
                        var.callback
                );
            }
        }
    }

    private Method getMethod(Class declaring, String name, Class... params) throws NoSuchMethodException {
        Method m = declaring.getDeclaredMethod(name, params);

        Validate.isTrue(Modifier.isStatic(m.getModifiers()),
                "Update callback '%s' was not static", name
        );

        m.setAccessible(true);
        return m;
    }

    private static IllegalArgumentException exception(String format, Object... args) {
        return new IllegalArgumentException(String.format(format, args));
    }

    record AnnotationData(String type, String prefix, String callback) {
        static AnnotationData combine(Var classAnnot, Var fieldAnnot) {
            String type = "";
            String prefix = "";
            String callback = "";

            if (classAnnot != null) {
                prefix = classAnnot.namePrefix();
            }

            if (fieldAnnot != null) {
                type = fieldAnnot.type();
                callback = fieldAnnot.callback();

                if (!fieldAnnot.namePrefix().isBlank()) {
                    prefix += fieldAnnot.namePrefix();
                }
            }

            return new AnnotationData(type, prefix, callback);
        }
    }
}