package net.forthecrown.core.script2;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.io.PathUtil;
import org.apache.logging.log4j.Logger;
import org.openjdk.nashorn.api.scripting.NashornScriptEngine;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

@Getter
public class Script implements Closeable {

  public static final String
      METHOD_ON_CLOSE = "__onClose";

  private static final Logger LOGGER = FTC.getLogger();

  /**
   * The file the script is loaded from
   */
  private final Path file;

  /**
   * name of the script
   */
  private final String name;

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

  /**
   * Script's self instance
   */
  private ScriptObjectMirror mirror;

  /**
   * Result of the initial script evaluation
   */
  private Object evalResult;

  /* ---------------------------- CONSTRUCTORS ---------------------------- */

  private Script(Path file) {
    this.file = file;
    this.name = deriveName(file);

    if (!Files.isRegularFile(file)) {
      throw Util.newException(
          "Script file '%s' is not a file!",
          file
      );
    }
  }

  /**
   * Derives the script's name from the given path.
   * <p>
   * If the script is in the script directory, then it's relative path inside the script directory
   * is returned, otherwise, the script's entire path is returned in string form
   */
  private static String deriveName(Path path) {
    var str = path.toString();
    var scriptDirectory = ScriptManager.getInstance()
        .getDirectory();

    if (str.contains(scriptDirectory.toString())) {
      return scriptDirectory.relativize(path).toString();
    }

    return path.toString();
  }

  /* ------------------------ STATIC CONSTRUCTORS ------------------------- */

  /**
   * Creates a script instance with the given name as the script's path within the plugin script
   * directory.
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
    return new Script(path);
  }

  /**
   * Reads the script file with the given name.
   * <p>
   * Script names are relative to the <code>plugins/ForTheCrown/scripts</code> directory, as an
   * example, a script used by a challenge might be called,
   * <code>challenges/on_challenge.js</code>. They must always include the
   * '.js' file extension as well.
   * <p>
   * Be aware! Since scripts can start scheduled tasks and register event listeners, it's optimal to
   * use a try-with-resources or other method to ensure that the {@link #close()} method is called
   * after the script is no longer needed to be in memory.
   *
   * @param file The script's name
   * @return The loaded script.
   * @throws ScriptLoadException      If an error occurred during script loading, script
   *                                  evaluation.
   * @throws IllegalArgumentException If the given file doesn't exist or isn't a file
   */
  public static Script read(String file)
      throws ScriptLoadException, IllegalArgumentException
  {
    return of(file).load();
  }

  /**
   * Reads the script file at the given path.
   * <p>
   * Be aware! Since scripts can start scheduled tasks and register event listeners, it's optimal to
   * use a try-with-resources or other method to ensure that the {@link #close()} method is called
   * after the script is no longer needed to be in memory.
   *
   * @param file The script's file
   * @return The loaded script
   * @throws ScriptLoadException      If an error occurred during script loading, script
   *                                  evaluation.
   * @throws IllegalArgumentException If the given file doesn't exist or isn't a file
   */
  public static Script read(Path file)
      throws ScriptLoadException, IllegalArgumentException
  {
    return of(file).load();
  }

  /**
   * Reads a script file using {@link #read(Path)} and then invokes the method with the given name
   * and arguments. After the function is executed, the script is closed
   *
   * @param file   The script file to invoke
   * @param method The name of the method to invoke
   * @param args   Arguments to pass to the function being invoked
   * @throws ScriptLoadException      If the script couldn't be loaded, see {@link #load()}
   * @throws IllegalArgumentException If the given script file didn't exist or wasn't a file
   */
  public static void run(Path file, String method, Object... args)
      throws ScriptLoadException, IllegalArgumentException
  {
    read(file).invoke(method, args).close();
  }

  /**
   * Reads a script file using {@link #read(String)} and then invokes the method with the given name
   * and arguments. After the function is executed, the script is closed
   *
   * @param f      The name of the script to invoke.
   * @param method The name of the method to invoke
   * @param args   Arguments to pass to the function being invoked
   * @throws ScriptLoadException      If the script couldn't be loaded, see {@link #load()}
   * @throws IllegalArgumentException If the given script file didn't exist or wasn't a file
   */
  public static void run(String f, String method, Object... args)
      throws ScriptLoadException, IllegalArgumentException
  {
    read(f).invoke(method, args).close();
  }

  /* ------------------------------ METHODS ------------------------------- */

  /**
   * Invokes the method with the given name and passes it the given arguments.
   * <p>
   * If the script is not loaded when this invocation is called, then {@link #load()} will be called
   * to load the script.
   * <p>
   * Any exception thrown by this method directly, will be wrapped with a script exception that
   * holds the name of the method, script and actual error to provide more detail.
   *
   * @param method The name of the method to invoke
   * @param args   The arguments to pass to the method.
   * @return The invocation result
   * @throws ScriptLoadException If the script was not already loaded and could not be loaded
   *                             either.
   */
  public ScriptResult invoke(String method, Object... args)
      throws ScriptLoadException {
    if (engine == null) {
      load();
    }

    return invokeSafe(this, mirror, method, args);
  }

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
   * Tests if the method with the given name exists, if it does, calls
   * {@link #invoke(String, Object...)}, if it doesn't, returns an empty optional
   *
   * @param method The name of the method to invoke
   * @param args   The arguments to pass to the method.
   * @return An empty optional, if the method didn't exist, an optional containing the invocation
   * result otherwise
   * @throws ScriptLoadException If the script was not already loaded and could not be loaded
   *                             either.
   */
  public Optional<ScriptResult> invokeIfExists(String method, Object... args)
      throws ScriptLoadException {
    if (!hasMethod(method)) {
      return Optional.empty();
    }

    return Optional.of(invoke(method, args));
  }

  /**
   * Loads this script from the script's file.
   * <p>
   * If this script has already been loaded, then {@link #close()} will be called before,
   * effectively reloading the script.
   * <p>
   * A script load exception will be thrown in one of the following scenarios:
   * <pre>
   * 1. If the script file doesn't exist.
   * 2. If the script file couldn't be read.
   * 3. If the script could not be 'compiled' aka evaluated
   * </pre>
   * If an exception is thrown, then {@link #close()} will be called again.
   * <p>
   * If the script is loaded and read correctly, then this script instance's fields are set and the
   * script itself is loaded.
   * <p>
   * On-top of just loading the script, this method also populates the script's bindings with the
   * default java classes, specified in {@link ScriptsBuiltIn}, and adds the {@link #getTasks()} and
   * {@link #getEvents()} to the script.
   *
   * @return This
   * @throws ScriptLoadException If the script couldn't be loaded
   */
  public Script load() throws ScriptLoadException {
    if (engine != null) {
      close();
    }

    try {
      var reader = Files.newBufferedReader(file);
      var engine = ScriptManager.getInstance().createEngine();

      // Add default values
      ScriptsBuiltIn.populate(name, engine);
      engine.put("scheduler", tasks);
      engine.put("events", events);
      engine.put("_script", this);

      this.engine = engine;
      this.mirror = (ScriptObjectMirror)
          engine.getBindings(ScriptContext.ENGINE_SCOPE);

      var ctx = engine.getContext();
      ctx.setAttribute(
          ScriptEngine.FILENAME,
          name,
          ScriptContext.ENGINE_SCOPE
      );

      evalResult = engine.eval(reader);
    } catch (IOException | ScriptException exc) {
      close();
      throw new ScriptLoadException(this, exc);
    }

    return this;
  }

  /**
   * Tests if this script has been loaded or not
   */
  public boolean isLoaded() {
    return mirror != null;
  }

  /**
   * Tests if this script has a method with the given name. If {@link #isLoaded()} is false, then
   * this returns false
   *
   * @param name The name of the method to look for
   * @return True, if a binding value with the given name existed, and it was a method
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
   * Unloads this script, cancels all event listeners created by this script, cancels all tasks
   * registered by this script and calls the {@link #METHOD_ON_CLOSE} method, if it exists.
   */
  @Override
  public void close() {
    if (engine == null) {
      return;
    }

    invokeIfExists(METHOD_ON_CLOSE);

    events.close();
    tasks.close();

    engine = null;
    mirror = null;
    evalResult = null;
  }

  /**
   * Gets the directory the script is inside
   */
  public Path getWorkingDirectory() {
    return file.getParent();
  }

  /**
   * Gets the script's data folder. The folder at the returned path may or may not exist.
   * <p>
   * A script's data folder is a folder with the same name as the script, with the file suffix
   * removed.
   *
   * @return This script's data folder
   */
  public Path getDataDirectory() {
    var path = getWorkingDirectory().resolve(
        file.getFileName()
            .toString()
            .replaceAll(".js", "")
    );

    PathUtil.ensureDirectoryExists(path).orThrow();
    return path;
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
    return getFile().equals(script.getFile())
        && Objects.equals(getEvalResult(), script.getEvalResult());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getFile(), getEvalResult());
  }

  @Override
  public String toString() {
    return name;
  }
}