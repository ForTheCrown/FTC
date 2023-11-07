package net.forthecrown.scripts.module;

import java.util.List;

public record ImportInfo(
    boolean isAlias,
    String path,
    String label,
    List<BindingImport> importedValues
) {

  public record BindingImport(String name, String alias) {
    public static final BindingImport STAR = new BindingImport("*", "*");
  }
}
