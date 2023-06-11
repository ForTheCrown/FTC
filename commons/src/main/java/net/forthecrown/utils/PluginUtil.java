package net.forthecrown.utils;

import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import java.lang.StackWalker.Option;
import java.util.Objects;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginUtil {
  private PluginUtil() {}

  /**
   * Delegate for {@link #getCallingPlugin(int)} with an effective depth of 2 (actually 3, to
   * compensate for the delegate method call)
   *
   * @return Calling plugin
   * @see #getCallingPlugin(int)
   *
   * @throws IllegalArgumentException Thrown by {@link JavaPlugin#getProvidingPlugin(Class)}
   * @throws IllegalStateException Thrown by {@link JavaPlugin#getProvidingPlugin(Class)}
   */
  public static JavaPlugin getCallingPlugin() {
    return getCallingPlugin(3);
  }

  /**
   * Gets the current plugin
   * <p>
   * Delegate for {@link #getCallingPlugin(int)} with an effective depth of 1 (actually 2, to
   * compensate for the delegate method call)
   *
   * @return Current plugin
   * @see #getCallingPlugin(int)
   *
   * @throws IllegalArgumentException Thrown by {@link JavaPlugin#getProvidingPlugin(Class)}
   * @throws IllegalStateException Thrown by {@link JavaPlugin#getProvidingPlugin(Class)}
   */
  public static JavaPlugin getPlugin() {
    return getCallingPlugin(2);
  }

  /**
   * Gets the calling plugin.
   * <p>
   * Use a depth of {@code 2} to determine who called the function this function is being
   * called from, for example, {@link Tasks} uses that depth to see what plugin a task should be
   * registered under
   * <p>
   * Use a depth of {@code 1} to determine what plugin called this function, aka, your own plugin
   * <p>
   * Any depth above 2 simply goes a layer up in the current call stack.
   * <p>
   * Internally, this gets a class at the specified {@code depth} in the call stack and calls
   * {@link JavaPlugin#getProvidingPlugin(Class)} to get its plugin
   *
   * @param depth Search depth
   * @return Calling plugin
   *
   * @throws IllegalArgumentException Thrown by {@link JavaPlugin#getProvidingPlugin(Class)}
   * @throws IllegalStateException Thrown by {@link JavaPlugin#getProvidingPlugin(Class)}
   */
  public static JavaPlugin getCallingPlugin(int depth)
      throws IllegalArgumentException, IllegalStateException
  {
    // +1 to compensate for the extra call layer
    var callerClass = StackLocatorUtil.getCallerClass(depth + 1);
    return JavaPlugin.getProvidingPlugin(callerClass);
  }

  /**
   * Gets the first plugin to appear in the current stack frame, will 100% return the plugin
   * that loaded this utility class lol
   *
   * @return Current context plugin
   */
  public static JavaPlugin currentContextPlugin() {
    JavaPlugin firstPlugin = getFirstPluginCaller();
    return Objects.requireNonNull(firstPlugin, "Not in a plugin context");
  }

  /**
   * Gets the first plugin caller in the current call stack
   * @return First plugin caller in the current call stack, or {@code null}, if none found
   */
  public static JavaPlugin getFirstPluginCaller() {
    StackWalker walker = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    return walker.walk(stream -> {
      return stream.filter(stackFrame -> {
        var clazz = stackFrame.getDeclaringClass();
        return clazz.getClassLoader() instanceof ConfiguredPluginClassLoader;
      })
          .map(stackFrame -> JavaPlugin.getProvidingPlugin(stackFrame.getDeclaringClass()))
          .findFirst().orElse(null);
    });
  }
}