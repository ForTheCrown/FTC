package net.forthecrown.core.module;

import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;

import java.lang.annotation.Annotation;

public class ModuleServices {
    public static final Registry<ModuleService>
            SERVICES = Registries.newRegistry();

    public static final ModuleService
            SAVE = ModuleService.of(OnSave.class),
            RELOAD = ModuleService.of(OnLoad.class),
            ON_ENABLE = ModuleService.of(OnEnable.class),
            ON_DISABLE = ModuleService.of(OnDisable.class);

    public static final DayChange
            DAY_CHANGE = new DayChange();

    public static final AutoSave
            AUTO_SAVE = new AutoSave();

    static {
        SERVICES.register("save", SAVE);
        SERVICES.register("reload", RELOAD);
        SERVICES.register("on_enable", ON_ENABLE);
        SERVICES.register("on_disable", ON_DISABLE);
        SERVICES.register("day_change", DAY_CHANGE);
        SERVICES.register("auto_save", AUTO_SAVE);
    }

    public static void run(Class<? extends Annotation> type) {
        for (var s: SERVICES) {
            if (s.getAnnotationType() != type) {
                continue;
            }

            s.run();
        }
    }
}