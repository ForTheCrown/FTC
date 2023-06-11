package net.forthecrown.scripts;

import com.google.common.base.Strings;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.forthecrown.utils.Result;
import org.jetbrains.annotations.NotNull;
import org.slf4j.helpers.NOPLogger;

class ExecResultImpl<T> implements ExecResult<T> {

  private final T value;
  private final String message;
  private final String methodName;
  private final Script script;

  private final Throwable throwable;

  private String fullyFormattedMessage;

  private ExecResultImpl(
      T value,
      String message,
      String methodName,
      Script script,
      Throwable throwable
  ) {
    this.value = value;
    this.message = message;
    this.methodName = methodName;
    this.script = script;
    this.throwable = throwable;
  }

  static ExecResult<Object> success(Object value, Script script) {
    return new ExecResultImpl<>(value, null, null, script, null);
  }

  static ExecResult<Object> success(Object value, String methodName, Script script) {
    return new ExecResultImpl<>(value, null, methodName, script, null);
  }

  static ExecResult<Object> wrap(Throwable t, Script script) {
    var message = ScriptImpl.getMessage(t);
    return new ExecResultImpl<>(null, message, null, script, t);
  }

  static ExecResult<Object> wrap(Throwable t, String methodName, Script script) {
    var message = ScriptImpl.getMessage(t);
    return new ExecResultImpl<>(null, message, methodName, script, t);
  }

  static ExecResult<Object> error(String message, Script script) {
    return new ExecResultImpl<>(null, message, null, script, null);
  }

  static ExecResult<Object> error(String message, String methodName, Script script) {
    return new ExecResultImpl<>(null, message, methodName, script, null);
  }

  @Override
  public boolean isSuccess() {
    return message == null && throwable == null;
  }

  @Override
  public Optional<T> result() {
    return Optional.ofNullable(value);
  }

  @Override
  public Script script() {
    return script;
  }

  @Override
  public Optional<String> error() {
    if (isSuccess()) {
      return Optional.empty();
    }

    String message = formatError();
    return Optional.of(message);
  }

  @Override
  public Optional<String> methodName() {
    return Optional.ofNullable(methodName);
  }

  @Override
  public ExecResult<T> logError() {
    if (message == null && throwable == null) {
      return this;
    }

    var logger = script.getLogger();
    boolean throwablePresent = throwable != null;
    String logMessage = formatError();

    // Can be true during testing
    if (logger instanceof NOPLogger) {
      var output = System.err;
      output.printf("[%s] %s\n", script.getName(), logMessage);

      if (throwablePresent) {
        throwable.printStackTrace(output);
      }
    } else if (throwablePresent) {
      logger.error(logMessage, throwable);
    } else {
      logger.error(logMessage);
    }

    return this;
  }

  @Override
  public void throwIfError() throws IllegalStateException {
    if (message == null && throwable == null) {
      return;
    }

    String message = formatError();

    if (throwable != null) {
      throw new IllegalStateException(message, throwable);
    } else {
      throw new IllegalStateException(message);
    }
  }

  private String formatError() {
    if (fullyFormattedMessage != null) {
      return fullyFormattedMessage;
    }

    boolean methodNamePresent = !Strings.isNullOrEmpty(methodName);

    Object[] arguments = new Object[methodNamePresent ? 2 : 1];

    if (methodNamePresent) {
      arguments[0] = methodName;
      arguments[1] = message;
    } else {
      arguments[0] = message;
    }

    String format;

    if (methodNamePresent) {
      format = "Error invoking method '%s': %s";
    } else {
      format = "Error evaluating script: %s";
    }

    fullyFormattedMessage = format.formatted(arguments);
    return fullyFormattedMessage;
  }

  @Override
  public @NotNull <V> ExecResult<V> map(@NotNull Function<T, V> function) {
    Objects.requireNonNull(function);

    if (value == null) {
      return (ExecResult<V>) this;
    }

    V newValue = function.apply(value);
    Objects.requireNonNull(newValue);

    return new ExecResultImpl<>(newValue, message, methodName, script, throwable);
  }

  @Override
  public <V> ExecResult<V> flatMap(@NotNull Function<T, Result<V>> mapper) {
    Objects.requireNonNull(mapper);

    if (value == null) {
      return (ExecResult<V>) this;
    }

    Result<V> newResult = mapper.apply(value);
    Objects.requireNonNull(newResult);

    if (newResult.isError()) {
      return new ExecResultImpl<>(null, newResult.getError(), methodName, script, throwable);
    } else {
      return new ExecResultImpl<>(newResult.getValue(), message, methodName, script, throwable);
    }
  }
}