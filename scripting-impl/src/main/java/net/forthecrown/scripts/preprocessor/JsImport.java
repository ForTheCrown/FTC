package net.forthecrown.scripts.preprocessor;

import com.google.common.base.Strings;
import com.mojang.datafixers.util.Unit;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.scripts.Script;
import net.forthecrown.scripts.module.ImportInfo;
import net.forthecrown.scripts.module.ImportInfo.BindingImport;
import net.forthecrown.scripts.module.ModuleManager;
import net.forthecrown.utils.Result;

@Getter
@RequiredArgsConstructor
class JsImport implements PreProcessorCallback {

  private final boolean aliased;
  private final String path;
  private final String alias;
  private final List<BindingImport> importedValues;

  String getBindingName() {
    if (!Strings.isNullOrEmpty(alias)) {
      return alias;
    }

    String res;

    if (path.endsWith(".js")) {
      res = path.substring(0, path.length() - 3);
    } else {
      res = path;
    }

    int slashIndex = res.lastIndexOf('/');
    int dotIndex = res.lastIndexOf('.');

    if (dotIndex != -1) {
      return res.substring(dotIndex + 1);
    }

    if (slashIndex != -1) {
      return res.substring(slashIndex + 1);
    }

    return res;
  }

  @Override
  public Result<Unit> postProcess(Script script) {
    ImportInfo info = new ImportInfo(aliased, path, getBindingName(), importedValues);
    ModuleManager manager = script.getService().getModules();

    return manager.importInto(script, info)
        .mapError(string -> "Import failure on script " + script.getName() + ": " + string);
  }
}
