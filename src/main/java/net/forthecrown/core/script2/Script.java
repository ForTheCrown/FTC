package net.forthecrown.core.script2;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import lombok.AccessLevel;
import lombok.Getter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.script2.ScriptSource.FileSource;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.openjdk.nashorn.api.scripting.NashornScriptEngine;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

@Getter
public class Script implements Closeable {

  public static final String
      METHOD_ON_CLOSE = "__onClose";

  private static final Logger LOGGER = Loggers.getLogger();

  /**
   * The source where the script originates, could be raw JS code, or a file's
   * path
   */
  private final ScriptSource source;

  /**
   * Script's event handler
   */
  private final ScriptEvents events = new ScriptEvents(this);

  /**
   * Script's tasks handler
   */
  private final ScriptTasks tasks = new ScriptTasks(this);

  /**
   * Script engine
   */
  private NashornScriptEngine engine;

  /** Compiled representation of this script */
  private CompiledScript compiledScript;

  /** Script's self instance */
  private ScriptObjectMirror mirror;

  @Getter(AccessLevel.PACKAGE)
  private final Set<WrappedScript> loadedSubScripts = new ObjectOpenHashSet<>();

  /* ---------------------------- CONSTRUCTORS ---------------------------- */

  private Script(ScriptSource source) {
    this.source = Objects.requireNonNull(source);
  }

  /* ------------------------ STATIC CONSTRUCTORS ------------------------- */

  /**
   * Creates a script instance with the given name as the script's path within
   * the plugin script directory.
   *
   * @param name The script's name
   * @return The created script
   * @throws IllegalArgumentException If the name doesn't end in '.js'
   */
  public static Script of(String name) throws IllegalArgumentException {
    return of(
        ScriptManager.getInstance()
            .getScriptFile(name)
    );
  }

  /**
   * Creates a script instance with the given path
   *
   * @param path The path the script file is at
   * @return The created script
   */
  public static Script of(Path path) {
    return new Script(ScriptSource.of(path));
  }

  public static Script ofCode(String code) {
    return new Script(ScriptSource.of(code));
  }

  public static Script of(ScriptSource source) {
    return new Script(source);
  }

  /**
   * Reads the script file with the given name.
   * <p>
   * Script names are relative to the <code>plugins/ForTheCrown/scripts</code>
   * directory, as an example, a script used by a challenge might be called,
   * <code>challenges/on_challenge.js</code>. They must always include the
   * '.js' file extension as well.
   * <p>
   * Be aware! Since scripts can start scheduled tasks and register event
   * listeners, it's optimal to use a try-with-resources or other method to
   * ensure that the {@link #close()} method is called after the script is no
   * longer needed to be in memory.
   *
   * @param file The script's name
   * @return The loaded script.
   * @throws ScriptLoadException      If an error occurred during script
   *                                  loading, script evaluation.
   * @throws IllegalArgumentException If the given file doesn't exist or isn't a
   *                                  file
   */
  public static Script read(String file)
      throws ScriptLoadException, IllegalArgumentException {
    return of(file).load();
  }

  /**
   * Reads the script file at the given path.
   * <p>
   * Be aware! Since scripts can start scheduled tasks and register event
   * listeners, it's optimal to use a try-with-resources or other method to
   * ensure that the {@link #close()} method is called after the script is no
   * longer needed to be in memory.
   *
   * @param file The script's file
   * @return The loaded script
   * @throws ScriptLoadException      If an error occurred during script
   *                                  loading, script evaluation.
   * @throws IllegalArgumentException If the given file doesn't exist or isn't a
   *                                  file
   */
  public static Script read(Path file)
      throws ScriptLoadException, IllegalArgumentException {
    return of(file).load();
  }

  /**
   * Reads a script file using {@link #read(Path)} and then invokes the method
   * with the given name and arguments. After the function is executed, the
   * script is closed
   *
   * @param file   The script file to invoke
   * @param method The name of the method to invoke
   * @param args   Arguments to pass to the function being invoked
   * @throws ScriptLoadException      If the script couldn't be loaded, see
   *                                  {@link #load(String...)}
   * @throws IllegalArgumentException If the given script file didn't exist or
   *                                  wasn't a file
   */
  public static void run(Path file, String method, Object... args)
      throws ScriptLoadException, IllegalArgumentException {
    read(file).invoke(method, args).close();
  }

  /**
   * Reads a script file using {@link #read(String)} and then invokes the method
   * with the given name and arguments. After the function is executed, the
   * script is closed
   *
   * @param f      The name (path) of the script to invoke.
   * @param method The name of the method to invoke
   * @param args   Arguments to pass to the function being invoked
   * @throws ScriptLoadException      If the script couldn't be loaded, see
   *                                  {@link #load(String...)}
   * @throws IllegalArgumentException If the given script file didn't exist or
   *                                  wasn't a file
   */
  public static void run(String f, String method, Object... args)
      throws ScriptLoadException, IllegalArgumentException {
    read(f).invoke(method, args).close();
  }

  /* ------------------------------ READING ------------------------------ */

  public static Script read(JsonElement element, boolean assumeRawJs) {
    ScriptSource source = ScriptSource.readSource(element, assumeRawJs);
    return new Script(source);
  }

  /* ------------------------------ METHODS ------------------------------- */

  static ScriptResult invokeSafe(Script script,
                                 ScriptObjectMirror thiz,
                                 String method,
                                 Object... args
  ) {
    var engine = script.getEngine();

    try {
      var result = engine.invokeMethod(thiz, method, args);

      return ScriptResult.builder()
          .result(result)
          .script(script)
          .method(method)
          .build();
    } catch (Exception e) {
      return ScriptResult.builder()
          .exception(e)
          .script(script)
          .method(method)
          .build()
          .logIfError();
    }
  }

  /**
   * Invokes the method with the given name and passes it the given arguments.
   * <p>
   * If the script is not loaded when this invocation is called, then
   * {@link #load(String...)} will be called to load the script.
   * <p>
   * Any exception thrown by this method directly, will be wrapped with a script
   * exception that holds the name of the method, script and actual error to
   * provide more detail.
   *
   * @param method The name of the method to invoke
   * @param args   The arguments to pass to the method.
   * @return The invocation result
   * @throws ScriptLoadException If the script was not already loaded and could
   *                             not be loaded either.
   */
  public ScriptResult invoke(String method, Object... args)
      throws ScriptLoadException {
    if (engine == null) {
      load();
    }

    return invokeSafe(this, mirror, method, args);
  }

  /**
   * Tests if the method with the given name exists, if it does, calls
   * {@link #invoke(String, Object...)}, if it doesn't, returns an empty
   * optional
   *
   * @param method The name of the method to invoke
   * @param args   The arguments to pass to the method.
   * @return An empty optional, if the method didn't exist, an optional
   * containing the invocation result otherwise
   * @throws ScriptLoadException If the script was not already loaded and could
   *                             not be loaded either.
   */
  public Optional<ScriptResult> invokeIfExists(String method, Object... args)
      throws ScriptLoadException {
    if (!hasMethod(method)) {
      return Optional.empty();
    }

    return Optional.of(invoke(method, args));
  }

  /**
   * Delegate for {@link #load(Consumer, String...)} with a null consumer
   *
   * @return This
   * @throws ScriptLoadException If the script couldn't be loaded
   * @see #load(Consumer, String...)
   */
  @Deprecated
  public Script load(String... args) throws ScriptLoadException {
    return load(null, args);
  }

  /**
   * Loads this script from the script's file.
   * <p>
   * If this script has already been loaded, then {@link #close()} will be
   * called before, effectively reloading the script.
   * <p>
   * A script load exception will be thrown in one of the following scenarios:
   * <pre>
   * 1. If the script file doesn't exist.
   * 2. If the script file couldn't be read.
   * 3. If the script could not be 'compiled' and then evaluated
   * </pre>
   * If an exception is thrown, then {@link #close()} will be called again.
   * <p>
   * If the script is loaded and read correctly, then this script instance's
   * fields are set and the script itself is loaded.
   * <p>
   * On-top of just loading the script, this method also populates the script's
   * bindings with the default java classes, specified in
   * {@link ScriptsBuiltIn}, and adds the {@link #getTasks()} and
   * {@link #getEvents()} to the script.
   *
   * @param loadCallback Callback for placing values into the script's bindings
   *                     before evaluation
   * @return This
   * @throws ScriptLoadException If the script couldn't be loaded
   * @deprecated This method performs 2 operations at once, compilation and
   * evaluation of a script, use {@link #compile(String...)} to load and compile
   * a script, and then use {@link #eval()} to evaluate
   */
  @Deprecated
  public Script load(@Nullable Consumer<Bindings> loadCallback, String... args)
      throws ScriptLoadException {
    compile(args);

    if (loadCallback != null) {
      loadCallback.accept(mirror);
    }

    var result = eval();

    if (result.error().isPresent()) {
      throw new ScriptLoadException(this, result.error().get());
    }

    return this;
  }

  /**
   * Loads, reads and compiles the script.
   * <p>
   * This will throw a script load exception in one of the following
   * circumstances: <pre>
   * 1. The file doesn't exist
   * 2. The file has syntax errors or contains
   *    tokens not supported by the Nashorn engine </pre>
   * If an exception is thrown, then {@link #close()} will be called again
   * <p>
   * After compilation, users of this class are free to edit the script bindings
   * however they deem fit. Script bindings can be accessed with
   * {@link #getMirror()}
   *
   * @param args Optional arguments to supply to the engine
   * @return This
   * @throws ScriptLoadException If the script couldn't be loaded
   */
  public Script compile(String... args) throws ScriptLoadException {
    if (isCompiled()) {
      close();
    }

    try {
      Reader reader = source.openReader();
      reader = JsPreProcessor.preprocess(reader);

      NashornScriptEngine engine
          = ScriptManager.getInstance().createEngine();

      engine.getContext().setAttribute(
          ScriptEngine.FILENAME,
          source.getName(),
          ScriptContext.ENGINE_SCOPE
      );

      // Add default values
      ScriptsBuiltIn.populate(source.getName(), engine);
      engine.put("scheduler", tasks);
      engine.put("events", events);
      engine.put("args", args);
      engine.put("_script", this);
      engine.put("workingDirectory", getWorkingDirectory());

      engine.put("compile", ScriptsBuiltIn.compileFunction(this));

      this.engine = engine;
      this.mirror = (ScriptObjectMirror)
          engine.getBindings(ScriptContext.ENGINE_SCOPE);

      compiledScript = engine.compile(reader);
    } catch (IOException | ScriptException exc) {
      close();
      throw new ScriptLoadException(this, exc);
    }

    return this;
  }

  /**
   * Evaluates this script. This requires the script to be compiled, with
   * {@link #compile(String...)}
   * <p>
   * Aka, runs the main scope of this script.
   *
   * @return A successful or failed result
   * @throws NullPointerException If the script is not compiled
   */
  public ScriptResult eval() throws NullPointerException {
    ensureCompiled();

    try {
      var obj = compiledScript.eval(mirror);

      return ScriptResult.builder()
          .script(this)
          .result(obj)
          .build();
    } catch (Exception t) {
      return ScriptResult.builder()
          .script(this)
          .result(null)
          .exception(t)
          .build()
          .logIfError();
    }
  }

  /**
   * Tests if this script has been loaded or not
   */
  public boolean isCompiled() {
    return compiledScript != null;
  }

  /**
   * Tests if this script has a method with the given name. If
   * {@link #isCompiled()} is false, then this returns false
   *
   * @param name The name of the method to look for
   * @return True, if a binding value with the given name existed, and it was a
   * method
   */
  public boolean hasMethod(String name) {
    if (mirror == null) {
      return false;
    }

    var obj = mirror.get(name);

    if (!(obj instanceof ScriptObjectMirror m)) {
      return false;
    }

    return m.isFunction() || m.isStrictFunction();
  }

  /**
   * Ensures this script has been compiled, if it hasn't throws a null pointer
   * exception
   */
  private void ensureCompiled() throws NullPointerException {
    Objects.requireNonNull(compiledScript, "Script not compiled");
  }

  /**
   * Places an object into the script's bindings.
   * <p>
   * This allows you to use the given object within the script easily, for
   * example, placing a {@link net.forthecrown.user.User} instance into a script
   * with the key 'user' would allow you to use it like so:
   * <pre>
   * const afk = user.isAfk();
   * print(afk);
   * </pre>
   *
   * @param key The name of the binding
   * @param o   The binding's value
   * @throws NullPointerException If this script wasn't compiled
   */
  public void put(String key, Object o) throws NullPointerException {
    ensureCompiled();
    mirror.put(key, o);
  }

  /**
   * Unloads this script, cancels all event listeners created by this script,
   * cancels all tasks registered by this script and calls the
   * {@link #METHOD_ON_CLOSE} method, if it exists.
   */
  @Override
  public void close() {
    if (engine == null) {
      return;
    }

    invokeIfExists(METHOD_ON_CLOSE);

    loadedSubScripts.forEach(wrappedScript -> {
      wrappedScript.getScript().close();
    });
    loadedSubScripts.clear();

    events.close();
    tasks.close();

    engine = null;
    mirror = null;
    compiledScript = null;
  }

  /**
   * Gets the directory the script is inside
   */
  public Path getWorkingDirectory() {
    if (source instanceof FileSource source) {
      return source.getPath().getParent();
    }

    return ScriptManager.getInstance()
        .getDirectory();
  }

  public String getName() {
    return getSource().getName();
  }

  /* -------------------------- OBJECT OVERRIDES -------------------------- */

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Script script)) {
      return false;
    }
    return Objects.equals(source, script.getSource());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getSource());
  }

  @Override
  public String toString() {
    return source.getName();
  }
}