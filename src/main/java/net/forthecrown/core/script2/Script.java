package net.forthecrown.core.script2;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.script2.ScriptSource.FileSource;
import net.forthecrown.core.script2.preprocessor.JsPreProcessor;
import org.apache.logging.log4j.Logger;
import org.openjdk.nashorn.api.scripting.NashornScriptEngine;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

@Getter
public class Script implements Closeable {

  public static final String METHOD_ON_CLOSE = "__onClose";

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

  /** Scripts loaded by this script */
  @Getter(AccessLevel.PACKAGE)
  private final Set<WrappedScript> loadedSubScripts = new ObjectOpenHashSet<>();

  @Setter(AccessLevel.PACKAGE)
  private Script parentScript;

  /** Script's logger */
  private final Logger logger;

  /* ---------------------------- CONSTRUCTORS ---------------------------- */

  private Script(ScriptSource source) {
    this.source = Objects.requireNonNull(source);
    this.logger = Loggers.getLogger(source.getName());
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
   * {@link #compile(String...)} will be called to load the script and then
   * {@link #eval()} to evaluate the global scope.
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
      throws ScriptLoadException
  {
    if (engine == null) {
      compile().eval();
    }

    try {
      var result = engine.invokeMethod(mirror, method, args);

      return ScriptResult.builder()
          .result(result)
          .script(this)
          .method(method)
          .build();
    } catch (Exception e) {
      return ScriptResult.builder()
          .exception(e)
          .script(this)
          .method(method)
          .build()
          .logIfError();
    }
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
      throws ScriptLoadException
  {
    if (!hasMethod(method)) {
      return Optional.empty();
    }

    return Optional.of(invoke(method, args));
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
   * @param args Optional arguments to supply to the script, will be placed into
   *             a 'args' binding
   * @return This
   * @throws ScriptLoadException If the script couldn't be loaded
   */
  public Script compile(String... args) throws ScriptLoadException {
    if (isCompiled()) {
      close();
    }

    try {
      String input = JsPreProcessor.preprocess(source.openReader());

      NashornScriptEngine engine
          = ScriptManager.getInstance().createEngine();

      engine.getContext().setAttribute(
          ScriptEngine.FILENAME,
          source.getName(),
          ScriptContext.ENGINE_SCOPE
      );

      // Add default values
      engine.put("scheduler", tasks);
      engine.put("events", events);
      engine.put("args", args);
      engine.put("_script", this);
      engine.put("logger", getLogger());

      this.engine = engine;
      this.mirror = (ScriptObjectMirror)
          engine.getBindings(ScriptContext.ENGINE_SCOPE);

      this.compiledScript = engine.compile(input);

      ScriptsBuiltIn.populate(this);
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

  public void putCallback(String key, JsCallback callback) {
    CallbackWrapper wrapper = new CallbackWrapper(this, callback);
    put(key, wrapper);
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
    if (parentScript != null) {
      return parentScript.getWorkingDirectory();
    }

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