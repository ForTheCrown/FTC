package net.forthecrown.vikings.valhalla.data;

import com.google.gson.JsonObject;
import net.forthecrown.emperor.serializer.JsonSerializable;
import net.forthecrown.emperor.utils.JsonUtils;
import net.forthecrown.vikings.valhalla.active.RaidCell;
import net.forthecrown.vikings.valhalla.triggers.TriggerAction;
import net.forthecrown.vikings.valhalla.triggers.TriggerCheck;
import net.forthecrown.vikings.valhalla.triggers.TriggerType;
import org.bukkit.event.Event;

//The most literal storage class to ever exist
public class UnbuiltTrigger<E extends Event> implements JsonSerializable {
    public TriggerType type;
    public TriggerAction<E> action;
    public TriggerCheck<E> check;
    public RaidCell[] cells;

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();

        json.add("type", JsonUtils.serializeEnum(type));

        return json;
    }
}
