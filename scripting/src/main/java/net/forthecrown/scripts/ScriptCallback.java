package net.forthecrown.scripts;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mozilla.javascript.Scriptable;

/**
 * Dynamic-signature function that can be registered into {@link Script} instances with
 * {@link Script#put(String, Object)}
 */
@FunctionalInterface
public interface ScriptCallback {

  /**
   * Invokes the function
   *
   * @param script  Script invoking the function
   * @param invoker Current scope's 'this' object
   * @param params  Parameters
   *
   * @return Result, may be null
   */
  @Nullable
  Object invoke(@NotNull Script script, @Nullable Scriptable invoker, @NotNull Object... params);
}