package net.forthecrown.scripts.module;

import com.mojang.datafixers.util.Unit;
import java.util.Optional;
import net.forthecrown.scripts.Script;
import net.forthecrown.utils.Result;
import org.mozilla.javascript.Scriptable;

/**
 * Script module manager. Modules registered within this manager can be imported by scripts and
 * used.
 * <p>
 * Custom modules can be created by implementing {@link Scriptable} and registering a
 * {@link JsModule} instance that creates the implemented class.
 * <br>
 * Example: <pre><code>
 * class SomeModule extends ScriptableObject {
 *
 *   public static final JsModule MODULE = scope -> {
 *     SomeModule mod = new SomeModule();
 *     mod.setParentScope(scope);
 *     return mod;
 *   }
 *
 *   &#064;Override
 *   public Object get(String key, Scriptable start) {
 *     return NOT_FOUND;
 *   }
 * }
 * </code></pre>
 * Then register it with <pre><code>
 * ModuleManager manager = // ...
 * manager.addModule("some_module", SomeModule.MODULE);
 * </code></pre>
 */
public interface ModuleManager {

  /**
   * Adds a module that scripts can import
   *
   * @param name Module name
   * @param module Module
   *
   * @return Addition result, will be failure if {@code name} or {@code module} were null or a
   *        module with the specified name was already registered.
   */
  Result<Unit> addModule(String name, JsModule module);

  /**
   * Sets a module that has already been registered with {@link #addModule(String, JsModule)} to be
   * an 'auto' module.
   * <p>
   * Auto modules are modules which are automatically imported into each script on compilation
   *
   * @param name Module name
   * @param state {@code true}, to set auto imported, {@code false} otherwise
   *
   * @return A failed result if a module with {@code name} didn't exist or if the module's auto
   *         state was already the same as specified
   */
  Result<Unit> setAutoModule(String name, boolean state);

  /**
   * Gets a registered module.
   * @param name Module's name
   * @return Module optional
   */
  Optional<JsModule> getModule(String name);

  /**
   * Remove a module with the specified {@code name}. If the module is marked as 'auto-import', then
   * it will be removed from the auto-import list.
   *
   * @param moduleName Module's name
   */
  void remove(String moduleName);

  Result<Unit> importInto(Script script, ImportInfo info);
}
