package net.forthecrown.utils;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DataResult.PartialResult;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.NotNull;

public class Result<V> {

  private final V value;
  private final String error;

  private Result(V value, String error) {
    this.value = value;
    this.error = error;
  }

  /**
   * Creates a successful result with the specified {@code value}
   * @param value Result value
   * @return Created result
   */
  public static <V> Result<V> success(V value) {
    Objects.requireNonNull(value);
    return new Result<>(value, null);
  }

  /**
   * Creates a failure result with a specified message.
   * <p>
   * Uses {@link String#format(String, Object...)} for formatting the message
   * and arguments
   *
   * @param message Message format
   * @param args Message arguments
   * @return Created result
   */
  public static <V> Result<V> error(String message, Object... args) {
    Objects.requireNonNull(message, "Null message");
    return new Result<>(null, message.formatted(args));
  }

  public static <V> Result<V> fromDataResult(DataResult<V> result) {
    var either = result.get();

    if (either.left().isPresent()) {
      return success(either.left().get());
    }

    PartialResult<V> partial = either.right().get();
    String error = partial.message();

    return error(error);
  }

  /**
   * Gets the result's value
   * @return Result value, or {@code null}, if no value is present
   */
  public V getValue() {
    return value;
  }

  /**
   * Gets the result's error message
   * @return Error message, or {@code null}, if the result is a successful
   *         result
   */
  public String getError() {
    return error;
  }

  /**
   * Tests if this result is an error result.
   * @return {@code true}, if an error is present, {@code false} otherwise
   */
  public boolean isError() {
    return error != null;
  }

  /**
   * Transforms the result's error message
   * @param mapper Error transformation function
   * @return Result with a transformed error message, or {@code this}, if the result has no error
   */
  public Result<V> mapError(@NotNull UnaryOperator<String> mapper) {
    Objects.requireNonNull(mapper);

    if (isError()) {
      return Result.error(mapper.apply(error));
    }

    return this;
  }

  /**
   * Maps this result's value to a different type
   * @param mapper Mapping function
   * @return Mapped result, or {@code this}, if it's an error result
   */
  public <T> Result<T> map(Function<V, T> mapper) {
    if (value == null || mapper == null) {
      return (Result<T>) this;
    }

    T newValue = mapper.apply(value);
    Objects.requireNonNull(newValue);

    return Result.success(newValue);
  }

  /**
   * If this is an error result, returns {@code this}, otherwise returns the
   * result of the mapper function
   *
   * @param mapper Mapping function
   * @return {@code this}, if it's an error result, otherwise the value of the
   *         specified {@code mapper} is returned
   */
  public <T> Result<T> flatMap(Function<V, Result<T>> mapper) {
    if (value == null) {
      return (Result<T>) this;
    }

    var result = mapper.apply(value);
    Objects.requireNonNull(result);

    return result;
  }

  /**
   * Gets the result's value, or throws an exception with the specified
   * exception factory
   *
   * @param exceptionFunction Exception factory
   * @return Result value
   */
  public <T extends Throwable> V orThrow(Function<String, T> exceptionFunction) throws T {
    if (error != null) {
      throw exceptionFunction.apply(error);
    }

    return value;
  }

  /**
   * Gets the result's value or a default value
   *
   * @param value Default value
   * @return {@link #getValue()} if present, or the {@code value} parameter
   */
  public V orElse(V value) {
    return error != null ? value : this.value;
  }

  /**
   * Either report's this result to the specified {@code errors} logger or calls
   * the specified {@code consumer}, depending on if this is an error result or
   * a successful result
   *
   * @param errorConsumer Error consumer
   * @param consumer Value consumer
   */
  public void apply(Consumer<String> errorConsumer, Consumer<V> consumer) {
    if (error != null) {
      errorConsumer.accept(error);
      return;
    }

    consumer.accept(value);
  }

  public <T> Result<T> cast() {
    return (Result<T>) this;
  }

  public <O, T> Result<T> combine(
      Result<O> o,
      BinaryOperator<String> errorCombiner,
      BiFunction<O, V, T> combiner
  ) {
    boolean thisError = isError();
    boolean otherError = o.isError();

    if (thisError && otherError) {
      String combined = errorCombiner.apply(error, o.getError());
      return error(combined);
    } else if (thisError) {
      return cast();
    } else if (otherError) {
      return o.cast();
    }

    T combined = combiner.apply(o.getValue(), getValue());
    return success(combined);
  }

  public Result<V> withError(Result<?> result, BinaryOperator<String> errorCombiner) {
    boolean thisError = isError();
    boolean otherError = result.isError();

    if (thisError && otherError) {
      String combined = errorCombiner.apply(error, result.getError());
      return error(combined);
    } else if (thisError) {
      return cast();
    } else if (otherError) {
      return result.cast();
    }

    return cast();
  }
}