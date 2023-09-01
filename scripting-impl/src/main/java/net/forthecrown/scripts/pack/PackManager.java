package net.forthecrown.scripts.pack;

import java.nio.file.Path;
import lombok.Getter;
import net.forthecrown.scripts.ScriptService;
import net.forthecrown.scripts.ScriptManager;

@Getter
public class PackManager {

  private final Path directory;
  private final ScriptService service;

  public PackManager(ScriptManager service, Path directory) {
    this.service = service;
    this.directory = directory;
  }

  public void reload() {

  }

  public void close() {

  }
}
