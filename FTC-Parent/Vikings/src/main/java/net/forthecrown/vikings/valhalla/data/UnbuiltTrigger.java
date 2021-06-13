package net.forthecrown.vikings.valhalla.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.core.serializer.JsonSerializable;
import net.forthecrown.core.utils.JsonUtils;
import net.forthecrown.vikings.valhalla.triggers.TriggerAction;
import net.forthecrown.vikings.valhalla.triggers.TriggerCheck;
import net.forthecrown.vikings.valhalla.triggers.TriggerType;
import org.bukkit.event.Event;

//The most literal storage class to ever exist
public class UnbuiltTrigger<E extends Event> implements JsonSerializable {
    public final TriggerType type;

    public TriggerAction<E> action;
    public TriggerCheck<E> check;
    public boolean removeAfterExec;

    public UnbuiltTrigger(TriggerType type) {
        this.type = type;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();

        json.add("type", JsonUtils.serializeEnum(type));
        json.add("removeAfterExec", new JsonPrimitive(removeAfterExec));

        if(check != null) json.add("check", check.serialize());
        if(action != null) json.add("action", action.serialize());

        return json;
    }
}
