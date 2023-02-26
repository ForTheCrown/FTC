package net.forthecrown.useables;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.utils.ArrayIterator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

@Getter
@Accessors(chain = true)
public class UsageType<T extends UsageInstance> {
  /* ----------------------------- FIELDS ------------------------------ */

  private final Class<T> typeClass;

  ReflectionExecutable<T> tagLoader;
  ReflectionExecutable<T> parser;

  ReflectionExecutable<T> emptyConstructor;

  @Setter
  SuggestionProvider<CommandSource> suggests
      = (context, builder) -> Suggestions.empty();

  @Setter
  @Accessors(fluent = true)
  private boolean requiresInput;

  /* ----------------------------- CONSTRUCTORS ------------------------------ */

  private UsageType(Class<T> typeClass) {
    this.typeClass = typeClass;

    findConstructors(this);

    boolean requiresEmptyConstructor = tagLoader == null || parser == null;

    if (requiresEmptyConstructor && emptyConstructor == null) {
      throw new IllegalStateException(
          "Missing 1 or more constructors and missing an empty constructor"
      );
    }

    requiresInput = emptyConstructor != null;
  }

  /* ----------------------------- METHODS ------------------------------ */

  public static <T extends UsageInstance> UsageType<T> of(Class<T> clazz) {
    return new UsageType<>(clazz);
  }

  public T parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
    return orEmpty(parser, reader, source);
  }

  public T load(BinaryTag tag) throws CommandSyntaxException {
    return orEmpty(tagLoader, tag);
  }

  public T create() throws CommandSyntaxException {
    if (requiresInput()) {
      throw Exceptions.REQUIRES_INPUT;
    }

    return tryRun(this, emptyConstructor);
  }

  private T orEmpty(ReflectionExecutable<T> executable, Object... params)
      throws CommandSyntaxException
  {
    if (executable == null) {
      return tryRun(this, emptyConstructor);
    } else {
      return tryRun(this, executable, params);
    }
  }

  /* ----------------------------- HELPER ------------------------------ */

  private static <T extends UsageInstance> T tryRun(UsageType<T> type,
                                                    ReflectionExecutable<T> executable,
                                                    Object... params
  ) throws CommandSyntaxException {
    try {
      return executable.invoke(type, params);
    } catch (InvocationTargetException exc) {
      var cause = exc.getCause();

      if (cause instanceof CommandSyntaxException syntaxException) {
        throw syntaxException;
      }

      // Invocation target exception means the constructor or
      // method screwed up, and it's not our fault, so we throw
      // the cause of the exception
      throw new IllegalStateException(exc.getCause());
    } catch (ReflectiveOperationException e) {

      // Reflective exception, we screwed up, throw
      // exception itself and not the cause
      throw new IllegalStateException(e);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T extends UsageInstance> void findConstructors(UsageType<T> type) {
    var c = type.getTypeClass();

    for (var constructor : c.getDeclaredConstructors()) {
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

    for (var method : c.getDeclaredMethods()) {
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

  @SuppressWarnings("rawtypes")
  private static boolean validate(UsableConstructor loader,
                                  Executable executable,
                                  UsageType usageType
  ) {
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

  private static String joinClassArray(Class<?>[] arr) {
    StringJoiner joiner = new StringJoiner(", ");

    var it = ArrayIterator.unmodifiable(arr);
    while (it.hasNext()) {
      joiner.add(it.next().getSimpleName());
    }

    return joiner.toString();
  }

  /* -------------------------- OBJECT OVERRIDES -------------------------- */

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UsageType<?> usageType)) {
      return false;
    }

    return getTypeClass().equals(usageType.getTypeClass())
        && Objects.equals(getTagLoader(), usageType.getTagLoader())
        && Objects.equals(getParser(), usageType.getParser())
        && Objects.equals(getEmptyConstructor(), usageType.getEmptyConstructor());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getTypeClass(),
        getTagLoader(),
        getParser(),
        getEmptyConstructor()
    );
  }

  /* ---------------------------- SUB CLASSES ----------------------------- */

  interface ReflectionExecutable<T extends UsageInstance> {

    T invoke(UsageType<T> type, Object... params)
        throws ReflectiveOperationException, CommandSyntaxException;
  }
}