package net.forthecrown.scripts;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.forthecrown.utils.io.source.Source;

public class CachingLoaderImpl implements CachingScriptLoader {

  private final Map<Source, Script> map = new HashMap<>();

  private final ScriptService service;
  private final Path workingDir;

  public CachingLoaderImpl(ScriptService service) {
    this(service, service.getScriptsDirectory());
  }

  public CachingLoaderImpl(ScriptService service, Path workingDirectory) {
    this.service = service;
    this.workingDir = workingDirectory;
  }

  @Override
  public Script remove(Source source) {
    Objects.requireNonNull(source, "Null source");
    return map.remove(source);
  }

  @Override
  public Script loadScript(Source source) {
    Objects.requireNonNull(source, "Null source");
    Script script = map.get(source);

    if (script != null) {
      return script;
    }

    script = service.newScript(this, source);
    script.setWorkingDirectory(workingDir);

    map.put(source, script);

    return script;
  }

  @Override
  public void close() {
    for (Script value : map.values()) {
      value.close();
    }
    map.clear();
  }
}
