package net.forthecrown.scripts;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import net.forthecrown.utils.io.source.Source;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public interface Script extends AutoCloseable {

  Object NOT_FOUND = new Object() {
    @Override
    public int hashCode() {
      return 0;
    }

    @Override
    public String toString() {
      return "NOT_FOUND";
    }
  };

  /**
   * Gets the script source
   * @return Script source
   */
  @NotNull
  Source getSource();

  /**
   * Gets the script's name. Delegate for {@link Source#name()}
   * @return Script source's name
   */
  default String getName() {
    return getSource().name();
  }

  /**
   * Gets the string argument array passed to the script when it's compiled
   * @return String argument array, or an empty array if no arguments are set
   * @see #setArguments(String...)
   */
  @NotNull
  String[] getArguments();

  /**
   * Sets the string argument array.
   * <p>
   * This argument array can be accessed inside scripts using the {@code args} value, for example:
   * <pre><code>
   * if (args.length != 1) {
   *   throw "No argument set";
   * }
   *
   * let argumentValue = args[0];
   * </code></pre>
   * @param arguments Argument array
   * @return {@code this}
   */
  Script setArguments(@Nullable String... arguments);

  /**
   * Gets whether this script compiles to java bytecode or is interpreted
   * @return {@code true}, if this script compiles to java bytecode or is interpreted
   * @see #setClassGen(boolean)
   */
  boolean useClassGen();

  /**
   * Sets if this script is compiled to java bytecode or is interpreted.
   * <p>
   * Scripts that use {@code classGen} compile slower, but script execution is faster
   *
   * @implNote Changes the backing Rhino JS engine's {@code optimizationLevel} to either {@code 9}
   *           or {@code -1}, depending on the value of {@code classGen}
   *
   * @param classGen {@code true} to compile this script to bytecode, {@code false} to interpret
   *
   * @return {@code this}
   */
  Script setClassGen(boolean classGen);

  /**
   * Gets the script's logger
   * <p>
   * Can be accessed inside script's with the {@code logger} value, for example:
   * <pre><code> logger.info("Hello, world!"); </code></pre>
   *
   * @return Script's logger
   */
  Logger getLogger();

  /**
   * Gets the script's working directory.
   * <p>
   * In most circumstances, this will be the {@code scripts} directory in the server root
   *
   * @return Script's working directory
   */
  @NotNull
  Path getWorkingDirectory();

  /**
   * Sets the script's working directory
   * @param workingDirectory Working directory
   * @return {@code this}
   */
  Script setWorkingDirectory(@NotNull Path workingDirectory);

  /**
   * Tests if the script is compiled
   * @return {@code true}, if {@link #compile()} has been called and {@link #close()}
   *         has not been called, {@code false} otherwise
   */
  boolean isCompiled();

  /**
   * Gets a value in the script's bindings.
   *
   * @param bindingName Binding name
   * @return Binding value, or {@code null}, if the value is null, or {@link #NOT_FOUND} if the
   *         value doesn't exist
   *
   * @throws IllegalStateException If {@link #isCompiled()} returns false
   */
  Object get(@NotNull String bindingName) throws IllegalStateException;

  /**
   * Tests if the script's bindings contain a method with the specified {@code methodName}
   *
   * @param methodName Name of the method to test for
   *
   * @return {@code true}, if a binding value with the specified name exists, and it is a method,
   *         {@code false} otherwise
   *
   * @throws IllegalStateException If {@link #isCompiled()} returns false
   */
  boolean hasMethod(String methodName) throws IllegalStateException;

  /**
   * Puts a value into the script's bindings
   *
   * @param bindingName Binding name
   * @param binding     Binding value
   *
   * @return {@code this}
   *
   * @throws IllegalStateException If {@link #isCompiled()} returns false
   */
  Script put(@NotNull String bindingName, @Nullable Object binding)
      throws IllegalStateException;

  /**
   * Puts a {@code const} value into the script's bindings. This value cannot be removed
   *
   * @param bindingName Binding name
   * @param binding     Binding value
   *
   * @return {@code this}
   *
   * @throws IllegalStateException If {@link #isCompiled()} returns false
   */
  Script putConst(@NotNull String bindingName, @Nullable Object binding)
    throws IllegalStateException;

  /**
   * Removes a value from the script's bindings
   *
   * @param bindingName Binding name
   * @return Value of the removed binding, or {@code null}, if the binding didn't exist or
   *         was null
   *
   * @throws IllegalStateException If {@link #isCompiled()} returns false
   */
  Object remove(@NotNull String bindingName) throws IllegalStateException;

  /**
   * Imports a class, making it accessible to the script. Imported classes will persist between
   * {@link #compile()} and {@link #close()} calls
   *
   * @param clazz Class to import
   * @return {@code this}
   */
  Script importClass(@NotNull Class<?> clazz);

  /**
   * Compiles the script from the {@link #getSource()}.
   * <p>
   * This function will load the input from the {@link #getSource()} and then pass it to a
   * preprocessor that compiles ES6+ code to a version of javascript compatible with the
   * internal Rhino scripting engine
   *
   * @return {@code this}
   * @throws ScriptLoadException If the script failed to load, will always have a cause
   */
  Script compile() throws ScriptLoadException;

  /**
   * Evaluates the script's main scope
   * @return Evaluation result
   */
  ExecResult<Object> evaluate();

  /**
   * Invokes a function in the script's main scope
   *
   * @param methodName Function name
   * @param arguments Function arguments
   *
   * @return Invocation result
   */
  ExecResult<Object> invoke(String methodName, Object... arguments);

  /**
   * Gets the script that loaded this script
   * @return Parent script, or {@code null}, if there's no parent script
   */
  @Nullable
  Script getParent();

  /**
   * Gets an immutable list of scripts that were loaded by this script
   * @return Loaded scripts
   */
  ImmutableList<Script> getChildren();

  /**
   * Adds a child script
   * @param child Child script
   * @return {@code this}
   */
  Script addChild(Script child);

  /**
   * Removes a child script
   * @param child Child script
   * @return {@code this}
   */
  Script removeChild(Script child);

  /**
   * Gets a map of all extensions in a script
   * @return Script's active extensions
   */
  ImmutableMap<String, ScriptExtension> getExtensions();

  /**
   * Adds an extension to a script
   *
   * @param name Extension's name, will be used as the name of the extension's binding value
   * @param extension Extension
   *
   * @return {@code true}, if the extension was added, {@code false}, if the extension couldn't be
   *         added due to a naming conflict
   */
  boolean addExtension(String name, ScriptExtension extension);

  /**
   * Removes an extension with the specified {@code name}
   * @param name Extension name
   * @return Removed extension, or {@code null}, if an extension with the specified {@code name}
   *         didn't exist
   */
  ScriptExtension removeExtension(String name);

  /**
   * Closes the script.
   * <p>
   * Deletes the compiled script object, all script bindings and calls
   * {@link ScriptExtension#onScriptClose()} for all extensions and closes all child scripts
   */
  @Override
  void close();
}