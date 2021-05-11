package net.forthecrown.vikings.valhalla.utils;

public class TriggerableEvent {

    private final String name;
    private final TriggerExecutor onExecute;
    private final TriggerType type;
    private final TriggerCaller caller;

    public TriggerableEvent(String name, TriggerType type, TriggerCaller caller, TriggerExecutor onExecute){
        this.name = name;
        this.type = type;
        this.caller = caller;
        this.onExecute = onExecute;
    }

    public String getName() {
        return name;
    }

    public TriggerExecutor getOnExecute() {
        return onExecute;
    }

    public TriggerType getType() {
        return type;
    }

    public TriggerCaller getCaller() {
        return caller;
    }
}
