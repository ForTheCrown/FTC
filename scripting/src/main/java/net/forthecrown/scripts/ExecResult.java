package net.forthecrown.scripts;

import com.mojang.datafixers.util.Unit;
import java.util.Optional;
import java.util.function.Function;
import net.forthecrown.utils.Result;
import org.jetbrains.annotations.NotNull;

/**
 * Result of a script being evaluated or a script's function being invoked
 * @param <T> Result's value type
 */
public interface ExecResult<T> {

  /**
   * Gets the result value. The returned optional will be empty if the script execution that
   * generated this result ran into an error or if that execution returned a {@code null} or
   * {@code undefined} value.
   * <p>
   * Thus, to ensure a result was successful, instead of doing {@code result().isPresent()},
   * use {@code error().isEmpty()}
   *
   * @return Result value container
   */
  Optional<T> result();

  /**
   * Tests if the execution was a success
   * @return {@code true}, if no {@link #error()} is present, {@code false} otherwise
   */
  default boolean isSuccess() {
    return error().isEmpty();
  }

  /**
   * Script that created this result
   * @return Result's script
   */
  Script script();

  /**
   * Gets the fully formatter error message
   * @return Error message container
   */
  Optional<String> error();

  /**
   * Gets the method name that was executed, will be empty if this execution result was NOT created
   * by calling {@link Script#evaluate()} instead of {@link Script#invoke(String, Object...)}
   *
   * @return Method name optional
   */
  Optional<String> methodName();

  /**
   * Logs the {@link #error()} if it's present, otherwise, it does nothing
   * @return {@code this}
   */
  ExecResult<T> logError();

  /**
   * Maps this result's value to a different type, effectively invokes
   * {@link Optional#map(Function)} on the {@link #result()}
   *
   * @param function Mapping function
   * @return Mapped result
   */
  @NotNull
  <V> ExecResult<V> map(@NotNull Function<T, V> function);

  /**
   * Flat maps this result's value to another result
   * @param mapper Mapping function
   * @return Exec result
   */
  <V> ExecResult<V> flatMap(@NotNull Function<T, Result<V>> mapper);

  /**
   * Flat maps this result with a function that takes the script as input.
   * <p>
   * This can be used to chain script invocations like so: <code><pre>
   * Script script = // ...
   * script.evaluate()
   *   .flatMapScript(s -> s.invoke("foo"))
   *   .logError()</pre></code>
   * <p>
   * The specified {@code function} will only be run if {@link #isSuccess()} returns {@code true}
   * for this result
   * <p>
   * If the {@code function} returns {@code null}, then this method will return {@code this}
   *
   * @param function Function to map result value
   * @return Returned result
   */
  <V> ExecResult<V> flatMapScript(@NotNull Function<Script, ExecResult<V>> function);

  /**
   * Throws an {@link IllegalStateException} if this result is a failed result, determined by
   * {@link #isSuccess()}
   *
   * @throws IllegalStateException If this result is a failed result
   */
  void throwIfError() throws IllegalStateException;

  /**
   * Converts this execution result to a regular result.
   * <p>
   * If this result is a success (indicated with {@link #isSuccess()}) But the return value itself
   * is {@code null}, then this result will return the {@link Unit} instance
   *
   * @return Converted result
   */
  Result<T> toRegularResult();
}