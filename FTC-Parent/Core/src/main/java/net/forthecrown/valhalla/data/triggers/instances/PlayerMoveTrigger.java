package net.forthecrown.valhalla.data.triggers.instances;

import com.google.gson.JsonElement;
import net.forthecrown.valhalla.active.ActiveRaid;
import net.forthecrown.valhalla.data.triggers.TriggerInstance;
import net.minecraft.advancements.critereon.PlayerPredicate;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveTrigger implements TriggerInstance<PlayerMoveEvent> {
    private final PlayerPredicate predicate;

    public PlayerMoveTrigger(PlayerPredicate predicate) {
        this.predicate = predicate;
    }

    public PlayerPredicate getPredicate() {
        return predicate;
    }

    @Override
    public boolean test(PlayerMoveEvent event) {
        return false;
    }

    @Override
    public void execute(PlayerMoveEvent context, ActiveRaid raid) {

    }

    @Override
    public boolean removeAfterExec() {
        return false;
    }

    @Override
    public JsonElement serialize() {
        return null;
    }
}
