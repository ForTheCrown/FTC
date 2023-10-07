package net.forthecrown.scripts.pack;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PackMeta {

  private Path mainScript;
  private Path directory;

  private final List<String> requiredPlugins = new ArrayList<>();
  private final List<PackExport> exports = new ArrayList<>();
}
