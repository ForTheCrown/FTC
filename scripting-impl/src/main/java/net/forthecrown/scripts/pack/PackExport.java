package net.forthecrown.scripts.pack;

import com.google.common.base.Strings;
import java.nio.file.Path;
import java.util.List;
import lombok.Getter;

@Getter
public class PackExport {

  private final String name;

  private final Path scriptFile;
  private final List<Export> exports;

  public PackExport(String name, Path scriptFile, List<Export> exports) {
    this.name = name;
    this.scriptFile = scriptFile;
    this.exports = exports;
  }

  public record Export(String name, String alias) {

    public boolean hasAlias() {
      return !Strings.isNullOrEmpty(alias);
    }

    @Override
    public String toString() {
      if (Strings.isNullOrEmpty(alias)) {
        return name;
      }

      return name + " as " + alias;
    }
  }
}
