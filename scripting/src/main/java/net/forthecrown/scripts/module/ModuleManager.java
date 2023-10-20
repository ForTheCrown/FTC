package net.forthecrown.scripts.module;

import com.mojang.datafixers.util.Unit;
import java.util.Optional;
import net.forthecrown.scripts.Script;
import net.forthecrown.utils.Result;

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

  Optional<JsModule> getModule(String name);

  Result<Unit> importInto(Script script, ImportInfo info);

  void remove(String moduleName);
}
