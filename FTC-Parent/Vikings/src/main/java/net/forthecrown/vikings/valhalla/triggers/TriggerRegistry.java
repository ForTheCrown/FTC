package net.forthecrown.vikings.valhalla.triggers;

import net.forthecrown.core.registry.BaseRegistry;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.vikings.utils.ActionProvider;
import net.forthecrown.vikings.utils.CheckProvider;

public class TriggerRegistry {

    private final Registry<ActionProvider> actionRegistry;
    private final Registry<CheckProvider> checkRegistry;

    public TriggerRegistry() {
        actionRegistry = new BaseRegistry<>();
        checkRegistry = new BaseRegistry<>();
    }

    public void loadDefaults(){
    }

    public Registry<ActionProvider> actionRegistry() {
        return actionRegistry;
    }

    public Registry<CheckProvider> checkRegistry() {
        return checkRegistry;
    }
}
