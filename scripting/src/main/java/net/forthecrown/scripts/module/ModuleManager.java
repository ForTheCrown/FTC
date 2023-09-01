package net.forthecrown.scripts.module;

import com.mojang.datafixers.util.Unit;
import java.util.Optional;
import net.forthecrown.scripts.Script;
import net.forthecrown.utils.Result;

public interface ModuleManager {

  Result<Unit> addModule(String name, JsModule module);

  Optional<JsModule> getModule(String name);

  Result<Unit> importInto(Script script, ImportInfo info);

}
