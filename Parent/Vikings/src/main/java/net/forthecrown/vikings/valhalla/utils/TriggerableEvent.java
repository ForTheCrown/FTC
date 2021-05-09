package net.forthecrown.vikings.valhalla.utils;

import net.forthecrown.vikings.valhalla.RaidBattle;

import java.util.function.Consumer;

public class TriggerableEvent {

    private final String name;
    private final Consumer<RaidBattle> onExecute;
    private final TriggerType type;
    private final TriggerCaller caller;

    public TriggerableEvent(String name, TriggerType type, TriggerCaller caller, Consumer<RaidBattle> onExecute){
        this.name = name;
        this.type = type;
        this.caller = caller;
        this.onExecute = onExecute;
    }
}
