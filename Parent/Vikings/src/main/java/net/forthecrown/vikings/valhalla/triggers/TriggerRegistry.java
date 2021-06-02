package net.forthecrown.vikings.valhalla.triggers;

import net.forthecrown.emperor.registry.BaseRegistry;
import net.forthecrown.emperor.registry.Registry;

import java.util.function.Supplier;

public class TriggerRegistry {

    private final Registry<Supplier<TriggerAction<?>>> actionRegistry;
    private final Registry<Supplier<TriggerCheck<?>>> checkRegistry;

    public TriggerRegistry() {
        actionRegistry = new BaseRegistry<>();
        checkRegistry = new BaseRegistry<>();
    }

    public void loadDefaults(){
    }

    public Registry<Supplier<TriggerAction<?>>> actionRegistry() {
        return actionRegistry;
    }

    public Registry<Supplier<TriggerCheck<?>>> checkRegistry() {
        return checkRegistry;
    }
}
