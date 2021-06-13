package net.forthecrown.vikings.valhalla.triggers;

import net.forthecrown.vikings.valhalla.active.ActiveRaid;
import net.forthecrown.vikings.valhalla.active.RaidCell;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class TriggerableEvent<E extends Event> {

    private final boolean removeAfterExec;
    private final TriggerCheck<E> check;
    private final TriggerAction<E> action;
    private final RaidCell[] in;

    public TriggerableEvent(boolean removeAfterExec, TriggerCheck<E> check, TriggerAction<E> action, RaidCell[] in) {
        this.removeAfterExec = removeAfterExec;
        this.check = check;
        this.action = action;
        this.in = in;
    }

    public boolean test(Player player, ActiveRaid raid, E event){
        return check.check(player, raid, event);
    }

    public void trigger(Player player, ActiveRaid raid, E event){
        action.trigger(player, raid, event);
    }

    public boolean testAndRun(Player player, ActiveRaid raid, E event){
        if(test(player, raid, event)){
            trigger(player, raid, event);
            return true;
        }

        return false;
    }

    public RaidCell[] getIn() {
        return in;
    }

    public boolean removeAfterFirstExec() {
        return removeAfterExec;
    }

    public TriggerCheck<E> getCheck() {
        return check;
    }

    public TriggerAction<E> getAction() {
        return action;
    }
}
