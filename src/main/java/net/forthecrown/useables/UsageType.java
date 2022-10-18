package net.forthecrown.useables;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.utils.ArrayIterator;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.StringJoiner;

@Getter
@Accessors(chain = true)
public class UsageType<T extends UsageInstance> {
    /* ----------------------------- FIELDS ------------------------------ */

    private final Class<T> typeClass;

    ReflectionExecutable<T> jsonLoader;
    ReflectionExecutable<T> tagLoader;
    ReflectionExecutable<T> parser;

    ReflectionExecutable<T> emptyConstructor;

    @Setter
    SuggestionProvider<CommandSource> suggests = (context, builder) -> Suggestions.empty();

    /* ----------------------------- CONSTRUCTORS ------------------------------ */

    private UsageType(Class<T> typeClass) {
        this.typeClass = typeClass;

        findConstructors(this);

        boolean hasNullConstructor = jsonLoader == null
                || tagLoader == null
                || parser == null;

        if (hasNullConstructor && emptyConstructor == null) {
            throw new IllegalStateException(
                    "Missing 1 or more constructors and missing an empty constructor"
            );
        }
    }

    /* ----------------------------- METHODS ------------------------------ */

    public static <T extends UsageInstance> UsageType<T> of(Class<T> clazz) {
        return new UsageType<>(clazz);
    }

    public T parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return orEmpty(parser, reader, source);
    }

    public T load(Tag tag) throws CommandSyntaxException {
        return orEmpty(tagLoader, tag);
    }

    public T load(JsonElement element) throws CommandSyntaxException {
        return orEmpty(jsonLoader, element);
    }

    public T create() throws CommandSyntaxException {
        if (requiresInput()) {
            throw Exceptions.REQUIRES_INPUT;
        }

        return tryRun(this, emptyConstructor);
    }

    private T orEmpty(ReflectionExecutable<T> executable, Object... params) throws CommandSyntaxException {
        if (executable == null) {
            return tryRun(this, emptyConstructor);
        } else {
            return tryRun(this, executable, params);
        }
    }

    public boolean requiresInput() {
        return emptyConstructor == null;
    }

    /* ----------------------------- HELPER ------------------------------ */

    private static <T extends UsageInstance> T tryRun(UsageType<T> type,
                                                      ReflectionExecutable<T> executable,
                                                      Object... params
    ) throws CommandSyntaxException {
        try {
            return executable.invoke(type, params);
        } catch (InvocationTargetException exc) {

            // Invocation target exception means the constructor or
            // method screwed up and it's not our fault, so we throw
            // the cause of the exception
            throw new IllegalStateException(exc.getCause());
        } catch (ReflectiveOperationException e) {

            // Reflective exception, we screwed up, throw
            // exception itself and not the cause
            throw new IllegalStateException(e);
        }
    }

    private static <T extends UsageInstance> void findConstructors(UsageType<T> type) {
        var c = type.getTypeClass();

        for (var constructor: c.getDeclaredConstructors()) {
            var annotation = constructor.getAnnotation(UsableConstructor.class);

            if (annotation == null) {
                continue;
            }

            boolean typeFirst = validate(annotation, constructor, type);

            ReflectionExecutable<T> executable = (type1, params) -> {
                if (typeFirst) {
                    return (T) constructor.newInstance(params);
                } else {
                    return (T) constructor.newInstance(ArrayUtils.insert(0, params, type1));
                }
            };
            annotation.value().set(type, executable);
        }

        for (var method: c.getDeclaredMethods()) {
            UsableConstructor loader = method.getAnnotation(UsableConstructor.class);

            if (loader == null) {
                continue;
            }

            boolean typeFirst = validate(loader, method, type);

            ReflectionExecutable<T> executable = (type1, params) -> {
                if (typeFirst) {
                    return (T) method.invoke(null, params);
                } else {
                    return (T) method.invoke(null, ArrayUtils.insert(0, params, type1));
                }
            };
            loader.value().set(type, executable);
        }
    }

    private static boolean validate(UsableConstructor loader, Executable executable, UsageType usageType) {
        var type = loader.value();
        var declaring = executable.getDeclaringClass();

        Validate.isTrue(
                type.get(usageType) == null,

                "%s constructor for class %s is already set",
                type.name().toLowerCase(),
                declaring.getSimpleName()
        );

        Validate.isTrue(
                executable.getParameterCount() == type.getParams().length
                || executable.getParameterCount() == type.getTypedParams().length,

                "Invalid parameter length on usable constructor!"
        );

        boolean typeFirst = true;
        var params = executable.getParameterTypes();
        Class[] typeParams = type.getParams();

        if (params.length > 0
                && params[0] == UsageType.class
        ) {
            typeParams = type.getTypedParams();
            typeFirst = false;
        }

        Validate.isTrue(
                Arrays.equals(typeParams, params),

                "Invalid parameters on usable constructor: required=(%s), found=(%s)",
                joinClassArray(type.getParams()),
                joinClassArray(executable.getParameterTypes())
        );

        if (executable instanceof Method m) {
            Validate.isTrue(
                    m.getReturnType().equals(declaring),

                    "Invalid return type on constructor method: %s, requires: %s",
                    m.getReturnType().getSimpleName(),
                    declaring.getSimpleName()
            );

            Validate.isTrue(
                    Modifier.isStatic(m.getModifiers()),

                    "Field %s in class %s is not static",
                    m.getName(),
                    declaring.getSimpleName()
            );
        }

        return typeFirst;
    }

    private static String joinClassArray(Class[] arr) {
        StringJoiner joiner = new StringJoiner(", ");

        var it = ArrayIterator.unmodifiable(arr);
        while (it.hasNext()) {
            joiner.add(it.next().getSimpleName());
        }

        return joiner.toString();
    }

    /* ----------------------------- SUB CLASSES ------------------------------ */

    interface ReflectionExecutable<T extends UsageInstance> {
        T invoke(UsageType<T> type, Object... params) throws ReflectiveOperationException, CommandSyntaxException;
    }
}