package net.forthecrown.core.module;

import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;

public class ModuleServices {

  public static final Registry<ModuleService> SERVICES
      = Registries.newRegistry();

  public static final ModuleService RELOAD
      = ModuleService.of(OnLoad.class);

  public static final ModuleService ON_DISABLE
      = ModuleService.of(OnDisable.class);

  public static final DayChange
      DAY_CHANGE = new DayChange();

  public static final AutoSave
      SAVE = new AutoSave();

  static {
    SERVICES.register("save", SAVE);
    SERVICES.register("reload", RELOAD);
    SERVICES.register("on_disable", ON_DISABLE);
    SERVICES.register("day_change", DAY_CHANGE);
  }
}