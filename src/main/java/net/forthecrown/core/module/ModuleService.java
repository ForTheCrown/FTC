package net.forthecrown.core.module;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.utils.Util;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class ModuleService implements Runnable {

  private static final Logger LOGGER = Loggers.getLogger();

  private final Class<? extends Annotation> annotationType;

  @Getter
  private final List<Pair<Object, Method>> callbacks = new ObjectArrayList<>();

  private final Timing timing;

  public ModuleService(Class<? extends Annotation> annotationType) {
    this.annotationType = annotationType;
    timing = FTC.timing(annotationType.getSimpleName());
  }

  public static ModuleService of(Class<? extends Annotation> annotationType) {
    return new ModuleService(annotationType);
  }

  public <T> void addAll(@NotNull Class<T> type, @Nullable T instance) {
    Objects.requireNonNull(type, "Type");

    for (var m : Util.getAllMethods(type)) {
      if (!m.isAnnotationPresent(annotationType)) {
        continue;
      }

      var result = testMethod(m, instance);

      if (result.isPresent()) {
        LOGGER.error("Cannot add method {} in {} to {}: {}",
            m.getName(),
            m.getDeclaringClass().getName(),
            annotationType.getSimpleName(),
            result.get()
        );

        continue;
      }

      m.setAccessible(true);
      callbacks.add(Pair.of(instance, m));
    }
  }

  public void run() {
    if (getCallbacks().isEmpty()) {
      return;
    }

    int ran = 0;
    timing.startTiming();
    String timingPrefix = annotationType.getSimpleName() + "::";

    for (var pair : getCallbacks()) {
      Method callback = pair.getSecond();
      Object module = pair.getFirst();

      Object instance;
      String name = module == null
          ? callback.getDeclaringClass().getSimpleName()
          : module.getClass().getSimpleName();

      if (Modifier.isStatic(callback.getModifiers())) {
        instance = null;
      } else {
        instance = module;

        if (instance == null) {
          LOGGER.error("Cannot run {} in {}! Not static",
              callback.getName(),
              name
          );

          continue;
        }
      }

      try (Timing t = Timings.ofStart(FTC.getPlugin(), timingPrefix + name, timing)) {
        invoke(instance, callback);
        ++ran;

        LOGGER.debug(
            "Ran {} on {}.{}",
            annotationType.getSimpleName(),
            name,
            callback.getName()
        );
      } catch (Throwable t) {
        if (t instanceof InvocationTargetException exc) {
          t = exc.getCause();
        }

        LOGGER.error(
            "Couldn't invoke method '{}' in '{}'",
            callback.getName(),
            name,
            t
        );
      }
    }

    timing.stopTiming();
    LOGGER.info(
        "Ran {} on {} modules",
        annotationType.getSimpleName(),
        ran
    );
  }

  public void invoke(@Nullable Object instance,
                     @NotNull Method m
  ) throws Throwable {
    m.invoke(instance);
  }

  public Optional<String> testMethod(@NotNull Method m,
                                     @Nullable Object instance
  ) {
    if (m.getReturnType() != Void.TYPE) {
      return Optional.of(
          String.format(
              "Method requires void return type, found '%s'",
              m.getReturnType().getName()
          )
      );
    }

    if (!Modifier.isStatic(m.getModifiers()) && instance == null) {
      return Optional.of(
          String.format(
              "Method %s is not static and no object instance " +
                  "is given",

              m.getName()
          )
      );
    }

    return testParams(m);
  }

  public Optional<String> testParams(Method m) {
    if (m.getParameterCount() != 0) {
      return Optional.of(
          "Expected 0 params"
      );
    }

    return Optional.empty();
  }
}